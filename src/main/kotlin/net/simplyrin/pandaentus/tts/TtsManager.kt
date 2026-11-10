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

import net.dv8tion.jda.api.entities.Guild
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

	}

	/**
	 * 読み上げの状態 (サーバーごと)
	 */
	class Session(val textChannelId: Long)

	val openJTalk: OpenJTalk = OpenJTalk.fromConfig(this.instance.config)

	private val sessions = ConcurrentHashMap<Long, Session>()

	// 発言の順番どおりに読み上げるため、音声の生成は 1 スレッドで順に行う。溜まりすぎたら新しい発言は捨てる
	private val synthesizer = ThreadPoolExecutor(
		1, 1, 0L, TimeUnit.MILLISECONDS,
		ArrayBlockingQueue(MAX_PENDING),
		{ runnable -> Thread(runnable, "tts-synthesizer").apply { isDaemon = true } },
		ThreadPoolExecutor.DiscardPolicy()
	)

	fun getSession(guildId: Long): Session? {
		return this.sessions[guildId]
	}

	/**
	 * 読み上げを開始する。Bot がボイスチャンネルに参加していなければ、voiceChannel に参加する。
	 */
	fun start(guild: Guild, textChannelId: Long, voiceChannel: AudioChannel) {
		val audioManager = guild.audioManager
		if (audioManager.connectedChannel == null) {
			audioManager.openAudioConnection(voiceChannel)
			audioManager.setAutoReconnect(false)
			audioManager.setSelfDeafened(true)
		}

		// 読み上げ用の送信ハンドラを設定する
		this.instance.getGuildAudioPlayer(guild)

		this.sessions[guild.idLong] = Session(textChannelId)
	}

	/**
	 * 読み上げを停止する。音楽を再生していなければ、ボイスチャンネルからも切断する。
	 */
	fun stop(guild: Guild) {
		this.sessions.remove(guild.idLong)

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
			this.sessions.remove(guild.idLong)
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
