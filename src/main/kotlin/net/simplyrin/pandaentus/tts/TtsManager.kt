package net.simplyrin.pandaentus.tts

import java.io.File
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.simplyrin.pandaentus.PandaEntus

/**
 * テキストチャンネルの発言を、Bot が参加しているボイスチャンネルで読み上げる。
 *
 * - `!ps` を実行したテキストチャンネルが読み上げの対象になる (サーバーごとに 1 チャンネル)。もう一度実行すると停止する。
 * - 通話に参加していなくても、そのテキストチャンネルの発言は読み上げる。
 * - ユーザーごとに声 (種類・ピッチ・速さ) が変わる。[TtsVoice] を参照。
 */
class TtsManager(private val instance: PandaEntus) : ListenerAdapter() {

	companion object {

		// 読み上げ用に生成した音声ファイルを置くフォルダ (再生が終わったら削除する)
		val FOLDER = File("tts")

		// 音声の生成待ち、再生待ちの上限。これを超えるほど発言が溜まったときは、新しい発言を読み上げない
		private const val MAX_PENDING = 10

		// 読み上げ中のサーバーを data.yml に保存するキー。Bot を再起動したあとに、読み上げを再開するために使う
		private const val SESSIONS_KEY = "Tts.Sessions"

	}

	/**
	 * 読み上げの状態 (サーバーごと)
	 */
	class Session(val textChannelId: Long, val voiceChannelId: Long)

	val openJTalk: OpenJTalk = OpenJTalk.fromConfig(this.instance.config)

	private val sessions = ConcurrentHashMap<Long, Session>()

	// 発言の順番どおりに読み上げるため、音声の生成は 1 スレッドで順に行う。溜まりすぎたら新しい発言は捨てる
	private val synthesizer = ThreadPoolExecutor(
		1, 1, 0L, TimeUnit.MILLISECONDS,
		ArrayBlockingQueue(MAX_PENDING),
		{ runnable -> Thread(runnable, "tts-synthesizer").apply { isDaemon = true } },
		ThreadPoolExecutor.DiscardPolicy()
	)

	// Bot の終了処理中は、切断されても読み上げを終了した扱いにしない (次の起動で再開するため)
	@Volatile
	private var closing = false

	fun getSession(guildId: Long): Session? {
		return this.sessions[guildId]
	}

	/**
	 * Bot がボイスチャンネルに参加していなければ、voiceChannel に参加する。音声を再生する送信ハンドラも設定する。
	 */
	fun connect(guild: Guild, voiceChannel: AudioChannel) {
		val audioManager = guild.audioManager
		if (audioManager.connectedChannel == null) {
			audioManager.openAudioConnection(voiceChannel)
			audioManager.setAutoReconnect(false)
			audioManager.setSelfDeafened(true)
		}

		// 読み上げ用の送信ハンドラを設定する
		this.instance.getGuildAudioPlayer(guild)
	}

	/**
	 * 読み上げを開始する。Bot がボイスチャンネルに参加していなければ、voiceChannel に参加する。
	 */
	fun start(guild: Guild, textChannelId: Long, voiceChannel: AudioChannel) {
		this.connect(guild, voiceChannel)

		this.sessions[guild.idLong] = Session(textChannelId, voiceChannel.idLong)
		this.saveSession(guild.idLong, textChannelId, voiceChannel.idLong)
	}

	/**
	 * 読み上げを停止する。音楽を再生していなければ、ボイスチャンネルからも切断する。
	 */
	fun stop(guild: Guild) {
		this.removeSession(guild.idLong)

		val musicManager = this.instance.getGuildAudioPlayer(guild)
		musicManager.ttsScheduler.clear()

		if (musicManager.player.playingTrack == null) {
			guild.audioManager.closeAudioConnection()
		}
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (!event.isFromGuild || event.isWebhookMessage) {
			return
		}

		val author = event.author
		if (author.isBot) {
			return
		}

		val guild = event.guild
		val session = this.sessions[guild.idLong] ?: return
		if (session.textChannelId != event.channel.idLong) {
			return
		}

		if (TtsText.shouldSkip(event.message.contentRaw)) {
			return
		}

		// Bot が切断されていたら、読み上げは終了している
		if (guild.audioManager.connectedChannel == null) {
			this.removeSession(guild.idLong)
			return
		}

		val text = TtsText.normalize(event.message.contentDisplay) ?: return
		val voice = TtsVoice.forUser(author.idLong, this.openJTalk.voices) ?: return

		this.synthesizer.execute { this.synthesizeAndPlay(guild, text, voice) }
	}

	/**
	 * 通話から誰もいなくなったら、読み上げを終了する。
	 */
	override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
		val guild = event.guild
		if (!this.sessions.containsKey(guild.idLong)) {
			return
		}

		val botChannel = guild.audioManager.connectedChannel ?: return
		if (event.channelLeft?.idLong == botChannel.idLong && botChannel.members.none { !it.user.isBot }) {
			println("[" + guild.name + "@" + guild.id + "] 通話に誰もいなくなったため、読み上げを終了します")
			this.stop(guild)
		}
	}

	/**
	 * Bot の終了処理の前に呼ぶ。これ以降の切断では、保存している読み上げの状態を消さない。
	 */
	fun prepareShutdown() {
		this.closing = true
	}

	/**
	 * 再起動の前に読み上げていたサーバーに再接続して、読み上げを再開する。起動が終わったあとに 1 回だけ呼ぶ。
	 * ボイスチャンネルに誰もいない、チャンネルが消えているなどのときは、再開せずに保存した状態も消す。
	 */
	fun restoreSessions() {
		val data = this.instance.dataConfig
		val guildIds = synchronized(data) {
			if (data.contains(SESSIONS_KEY)) data.getSection(SESSIONS_KEY).keys.toList() else emptyList()
		}

		for (key in guildIds) {
			val guildId = key.toLongOrNull() ?: continue
			try {
				this.restoreSession(guildId)
			} catch (e: Exception) {
				e.printStackTrace()
				this.removeSession(guildId)
			}
		}
	}

	private fun restoreSession(guildId: Long) {
		val data = this.instance.dataConfig
		val textChannelId: Long
		val voiceChannelId: Long
		synchronized(data) {
			textChannelId = data.getLong("$SESSIONS_KEY.$guildId.TextChannel", 0L)
			voiceChannelId = data.getLong("$SESSIONS_KEY.$guildId.VoiceChannel", 0L)
		}

		val guild = this.instance.jda.getGuildById(guildId)
		val voiceChannel = guild?.getChannelById(AudioChannel::class.java, voiceChannelId)
		if (guild == null || voiceChannel == null) {
			println("[TTS] 読み上げを再開できません (サーバーまたはボイスチャンネルが見つかりません): $guildId")
			this.removeSession(guildId)
			return
		}

		if (voiceChannel.members.none { !it.user.isBot }) {
			println("[" + guild.name + "@" + guild.id + "] 通話に誰もいないため、読み上げを再開しません")
			this.removeSession(guildId)
			return
		}

		if (!this.openJTalk.isReady() || !guild.selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT)) {
			println("[" + guild.name + "@" + guild.id + "] 読み上げを再開できません (Open JTalk の未設定、または接続の権限がありません)")
			this.removeSession(guildId)
			return
		}

		this.start(guild, textChannelId, voiceChannel)
		println("[" + guild.name + "@" + guild.id + "] 再起動前の読み上げを再開しました: " + voiceChannel.name)

		guild.getChannelById(GuildMessageChannel::class.java, textChannelId)
			?.sendMessage("🔊 再起動したため、読み上げに再接続しました。(!ps で停止)")
			?.queue(null) { it.printStackTrace() }
	}

	private fun saveSession(guildId: Long, textChannelId: Long, voiceChannelId: Long) {
		val data = this.instance.dataConfig
		synchronized(data) {
			data.set("$SESSIONS_KEY.$guildId.TextChannel", textChannelId)
			data.set("$SESSIONS_KEY.$guildId.VoiceChannel", voiceChannelId)
		}

		this.instance.saveDataConfig()
	}

	private fun removeSession(guildId: Long) {
		this.sessions.remove(guildId)

		if (this.closing) {
			return
		}

		val data = this.instance.dataConfig
		val changed = synchronized(data) {
			if (data.contains("$SESSIONS_KEY.$guildId")) {
				data.set("$SESSIONS_KEY.$guildId", null)
				true
			} else {
				false
			}
		}

		if (changed) {
			this.instance.saveDataConfig()
		}
	}

	private fun synthesizeAndPlay(guild: Guild, text: String, voice: TtsVoice) {
		val musicManager = this.instance.getGuildAudioPlayer(guild)
		if (musicManager.ttsScheduler.pendingCount >= MAX_PENDING) {
			return
		}

		FOLDER.mkdirs()
		val file = File(FOLDER, UUID.randomUUID().toString() + ".wav")

		if (!this.openJTalk.synthesize(text, voice, file)) {
			println("[TTS] 音声の生成に失敗しました: $text")
			file.delete()
			return
		}

		this.enqueueFile(guild, file)
	}

	/**
	 * 音声ファイル (wav) を、読み上げと同じ経路で再生する。再生が終わったらファイルは削除する。
	 * ファイルは [FOLDER] の直下に置くこと。
	 */
	fun enqueueFile(guild: Guild, file: File) {
		val musicManager = this.instance.getGuildAudioPlayer(guild)

		// loadItemOrdered は、同じキーの読み込みを呼び出した順に実行する。そのため、発言の順番のまま再生される
		this.instance.playerManager.loadItemOrdered(musicManager, file.absolutePath, object : AudioLoadResultHandler {

			override fun trackLoaded(track: AudioTrack) {
				musicManager.ttsScheduler.queue(track)
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				file.delete()
			}

			override fun noMatches() {
				println("[TTS] 音声ファイルを読み込めませんでした: " + file.name)
				file.delete()
			}

			override fun loadFailed(exception: FriendlyException) {
				println("[TTS] 音声ファイルの読み込みに失敗しました: " + exception.message)
				file.delete()
			}

		})
	}

}
