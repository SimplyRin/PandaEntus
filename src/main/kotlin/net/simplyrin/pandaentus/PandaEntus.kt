package net.simplyrin.pandaentus

import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.text.DecimalFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.system.exitProcess

import com.google.common.reflect.ClassPath
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider

import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.AndroidVr
import dev.lavalink.youtube.clients.Ios
import dev.lavalink.youtube.clients.MWeb
import dev.lavalink.youtube.clients.Music
import dev.lavalink.youtube.clients.Tv
import dev.lavalink.youtube.clients.Web
import dev.lavalink.youtube.clients.WebEmbedded
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import net.simplyrin.config.Config
import net.simplyrin.config.Configuration
import net.simplyrin.pandaentus.audio.GuildMusicManager
import net.simplyrin.pandaentus.audio.TrackScheduler
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.ReactionMessage
import net.simplyrin.pandaentus.gamemanager.AkinatorManager
import net.simplyrin.pandaentus.gamemanager.VoiceChannelNameManager
import net.simplyrin.pandaentus.listeners.ActivityListener
import net.simplyrin.pandaentus.listeners.CommandExecutor
import net.simplyrin.pandaentus.listeners.Listener
import net.simplyrin.pandaentus.listeners.ReactionListener
import net.simplyrin.pandaentus.utils.PoolItems
import net.simplyrin.pandaentus.utils.ThreadPool
import net.simplyrin.pandaentus.utils.TimeManager
import net.simplyrin.pandaentus.utils.TimeUtils
import net.simplyrin.pandaentus.utils.Version
import net.simplyrin.rinstream.RinStream

/**
 * Created by SimplyRin on 2019/03/09.
 *
 * Copyright (C) 2019 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
class PandaEntus {

	companion object {

		const val BOT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36"

		@JvmStatic
		fun main(args: Array<String>) {
			PandaEntus().run(args)
		}

	}

	lateinit var config: Configuration
		private set
	lateinit var dataConfig: Configuration
		private set
	lateinit var activityConfig: Configuration
		private set

	lateinit var timeManager: TimeManager
		private set
	lateinit var poolItems: PoolItems
		private set
	lateinit var jda: JDA
		private set
	lateinit var timeUtils: TimeUtils
		private set
	lateinit var voiceTextApiKey: String
		private set
	val messages: MutableList<ReactionMessage> = ArrayList()

	lateinit var commandRegister: CommandExecutor
		private set

	val loopMap: MutableMap<Long, AudioTrack> = HashMap()
	val forceLoopMap: MutableMap<Long, AudioTrack> = HashMap()

	val previousTrack: MutableMap<Long, AudioTrack> = HashMap()

	lateinit var playerManager: AudioPlayerManager
		private set
	val musicManagers: MutableMap<Long, GuildMusicManager> = HashMap()
	lateinit var vcNameManager: VoiceChannelNameManager
		private set

	val startupDate = Date()
	val akiMap: MutableMap<String, AkinatorManager> = HashMap()

	var audioPlaylist: AudioPlaylist? = null

	lateinit var amongUsGuildList: MutableList<String>
		private set

	lateinit var eventListener: Listener
		private set
	lateinit var activityListener: ActivityListener
		private set

	val botUserAgent: String = BOT_USER_AGENT

	private var notice = false

	fun run(args: Array<String>) {
		val rinStream = RinStream()
		rinStream.setPrefix("yyyy/MM/dd (E) HH:mm:ss")
			// .enableError()
			.setSaveLog(true)
			.setEnableColor(true)
			.setEnableTranslateColor(true)

		if (Version.SHA == "") {
			println("あなたは現在開発者ビルドを使用しています。")
			println("PandaEntus の公式ビルドは GitHub Releases からダウンロードすることができます。")
			println("Releases: https://github.com/SimplyRin/PandaEntus/releases")

			try {
				Thread.sleep(1000)
			} catch (e: Exception) {
			}
		}

		if (args.isNotEmpty() && args[0].equals("-tail", ignoreCase = true)) {
			rinStream.tail()
			ThreadPool.run {
				try {
					Thread.sleep(1000)
				} catch (e: Exception) {
				}
				println("Tail enabled.")
			}
		}

		println("setSavingLog: true")

		println("Loading files...")
		println("Build-Version: " + Version.BUILD_TIME)

		val file = File("config.yml")
		if (!file.exists()) {
			try {
				file.createNewFile()
			} catch (e: IOException) {
				e.printStackTrace()
			}

			val newConfig = Config.getConfig(file)
			newConfig.set("Token", "BOT_TOKEN_HERE")
			newConfig.set("Admin-ID", "YOUR_ACCOUNT_ID_HERE")
			newConfig.set("Pastebin.API-Key", "PASTEBIN_API_KEY_HERE")
			newConfig.set("VoiceTextApiKey", "VOICETEXT_API_KEY_HERE")
			newConfig.set("Message-Type.Enable-Simple-Mode", false)

			Config.saveConfig(newConfig, file)
		}

		this.config = Config.getConfig(file)

		val dataFile = File("data.yml")
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile()
			} catch (e: IOException) {
				e.printStackTrace()
			}

			val newDataConfig = Config.getConfig(dataFile)
			newDataConfig.set("OutenMaster", 3)

			Config.saveConfig(newDataConfig, dataFile)
		}

		this.dataConfig = Config.getConfig(dataFile)

		val activityData = File("activity.yml")
		if (!activityData.exists()) {
			try {
				activityData.createNewFile()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}

		this.activityConfig = Config.getConfig(activityData)

		// パス再設定
		if (this.config.getString("Bot.Restart-Script", null) == null) {
			this.config.set("Bot.Restart-Script", "start.sh")
		}

		this.timeManager = TimeManager(this)
		this.poolItems = PoolItems(this)
		this.timeUtils = TimeUtils()
		this.voiceTextApiKey = this.config.getString("VoiceTextApiKey")
		this.amongUsGuildList = ArrayList()

		this.commandRegister = CommandExecutor(this)

		val token = this.config.getString("Token")
		if (token == "BOT_TOKEN_HERE") {
			println("Discord Bot Token を config.yml に入力してください！")
			exitProcess(0)
		}
		try {
			val jdaBuilder = JDABuilder.createDefault(token, GatewayIntent.values().toList())
			jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL)
			jdaBuilder.setChunkingFilter(ChunkingFilter.ALL)
			jdaBuilder.enableCache(CacheFlag.ACTIVITY)
			jdaBuilder.enableCache(CacheFlag.VOICE_STATE)
			this.jda = jdaBuilder.build().awaitReady()
		} catch (e: Exception) {
			e.printStackTrace()
		}
		this.jda.addEventListener(this.commandRegister)
		this.eventListener = Listener(this)
		this.jda.addEventListener(this.eventListener)
		this.jda.addEventListener(ReactionListener(this))
		this.activityListener = ActivityListener(this)
		this.jda.addEventListener(this.activityListener)

		// 自動登録
		try {
			println("コマンドを登録しています...")
			val commands = this.jda.updateCommands()

			val classLoader = Thread.currentThread().contextClassLoader
			for (classInfo in ClassPath.from(classLoader).topLevelClasses) {
				if (classInfo.name.startsWith("net.simplyrin.pandaentus.command")) {
					val baseCommand = Class.forName(classInfo.name).getDeclaredConstructor().newInstance() as? BaseCommand ?: continue
					this.commandRegister.registerCommand(commands, baseCommand.command, baseCommand)
				}
			}

			val commandList = commands.complete()
			println("コマンドを登録しました。スラッシュコマンド: " + commandList.size)
		} catch (e: Exception) {
			println("エラーが発生しました。")
			e.printStackTrace()
			return
		}

		try {
			println(this.jda.selfUser.name)
			for (guild in this.jda.guilds) {
				println(guild.name + "@" + guild.id)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		this.playerManager = DefaultAudioPlayerManager()
		this.playerManager.createPlayer()
		// AudioSourceManagers.registerRemoteSources(this.playerManager);
		// AudioSourceManagers.registerLocalSource(this.playerManager);
		this.playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault())
		this.playerManager.registerSourceManager(BandcampAudioSourceManager())

		// poToken / visitorData が設定されていれば Web 系クライアントに適用する。
		// 未設定だと YouTube の bot 検出に引っかかり "This video requires login" になりやすい。
		// 取得方法: https://github.com/iv-org/youtube-trusted-session-generator
		val poToken: String? = this.config.getString("YouTube.PoToken", null)
		val visitorData: String? = this.config.getString("YouTube.VisitorData", null)
		if (!poToken.isNullOrEmpty() && !visitorData.isNullOrEmpty()) {
			Web.setPoTokenAndVisitorData(poToken, visitorData)
			WebEmbedded.setPoTokenAndVisitorData(poToken, visitorData)
		}

		// デフォルトのクライアント一覧 (Music, AndroidVr, Web, WebEmbedded) には
		// OAuth 対応クライアントが 1 つも含まれておらず、リフレッシュトークンが使われない。
		// OAuth に対応しているのは Tv のみ (Tv はメタデータ取得不可・再生用途専用)。
		val youtube = YoutubeAudioSourceManager(true,
			Music(), AndroidVr(), Web(), WebEmbedded(), MWeb(), Ios(), Tv())

		val youtubeRefreshToken: String? = this.config.getString("YouTube.OAuth2.Refresh-Token", null)
		if (youtubeRefreshToken.isNullOrEmpty()) {
			// 初回起動時など、リフレッシュトークン未設定の場合はここでログに認証用URLとコードが出力される。
			// 認証完了後、同じくログに出力されるリフレッシュトークンを config.yml の
			// YouTube.OAuth2.Refresh-Token に保存して再起動すること。
			youtube.useOauth2(null, false)
		} else {
			youtube.useOauth2(youtubeRefreshToken, true)
		}

		// YouTube へのアクセスだけを別の IP (WireGuard 等) 経由にする。
		// プロキシは YoutubeAudioSourceManager 専用の HttpInterfaceManager にのみ適用されるため、
		// Discord / SoundCloud / Bandcamp 等の通信は素の回線のまま影響を受けない。
		// メタデータ取得だけでなく googlevideo.com からの音声ストリームも同じ経路を通る。
		val proxyHost: String? = this.config.getString("YouTube.Proxy.Host", null)
		val proxyPort = this.config.getInt("YouTube.Proxy.Port", 0)
		if (!proxyHost.isNullOrEmpty() && proxyPort > 0) {
			val proxy = HttpHost(proxyHost, proxyPort)
			youtube.httpInterfaceManager
				.configureRequests { requestConfig -> RequestConfig.copy(requestConfig).setProxy(proxy).build() }

			val proxyUser: String? = this.config.getString("YouTube.Proxy.User", null)
			if (!proxyUser.isNullOrEmpty()) {
				val proxyPassword = this.config.getString("YouTube.Proxy.Password", "")
				val credentials = BasicCredentialsProvider()
				credentials.setCredentials(AuthScope(proxy),
					UsernamePasswordCredentials(proxyUser, proxyPassword))
				youtube.httpInterfaceManager
					.configureBuilder { builder -> builder.setDefaultCredentialsProvider(credentials) }
			}

			println("YouTube へのアクセスを $proxyHost:$proxyPort 経由に設定しました。")
		}

		this.playerManager.registerSourceManager(youtube)

		this.vcNameManager = VoiceChannelNameManager(this)

		this.jda.presence.setActivity(Activity.playing("🐼"))

		this.addShutdownHook {
			this.jda.shutdown()
			Config.saveConfig(this.config, "config.yml")
			Config.saveConfig(this.dataConfig, "data.yml")
			Config.saveConfig(this.activityConfig, "activity.yml")
			this.poolItems.save()
			println("Config ファイルを保存しました。")

			rinStream.close()
		}
	}

	fun saveConfig() {
		Config.saveConfig(this.config, "config.yml")
	}

	fun saveDataConfig() {
		Config.saveConfig(this.dataConfig, "data.yml")
	}

	fun saveActivityConfig() {
		val activityData = File("activity.yml")
		Config.saveConfig(this.activityConfig, activityData)
	}

	fun getTextChannelCategory(guild: Guild): Category? {
		var list = guild.getCategoriesByName("Text Channels", true)
		if (list.isNullOrEmpty()) {
			list = guild.getCategoriesByName("テキストチャンネル", true)
		}

		return list.firstOrNull()
	}

	fun getVoiceChannelCategory(guild: Guild): Category? {
		var list = guild.getCategoriesByName("Voice Channels", true)
		if (list.isNullOrEmpty()) {
			list = guild.getCategoriesByName("ボイスチャンネル", true)
		}

		return list.firstOrNull()
	}

	fun getVoiceChannelName(category: Category): String {
		val name = category.name
		return if (name == "ボイスチャンネル") {
			"一般"
		} else {
			"General"
		}
	}

	fun addShutdownHook(runnable: Runnable) {
		Runtime.getRuntime().addShutdownHook(Thread(runnable))
	}

	fun mmssToSeconds(time: String): Int {
		val split = if (time.contains(":")) time.split(":") else time.split("：")
		val mm = split[0].toInt()
		val ss = split[1].toInt()

		return (mm * 60) + ss
	}

	fun getNowTime(): String {
		return this.getNowTime(Date())
	}

	@Suppress("DEPRECATION")
	fun getNowTime(date: Date): String {
		return date.hours.toString() + "時" + date.minutes + "分"
	}

	fun getUptime(createdTime: Date): String {
		/**
		 * https://teratail.com/questions/28238
		 */
		val time = this.getTimeFromDate(createdTime)

		var uptime = ""
		val year = time.year
		val month = time.month
		val day = time.day
		val hour = time.hour
		val minute = time.minute
		val second = time.second

		if (year > 0) {
			uptime += year.toString() + "年"
		}
		if (month > 0) {
			uptime += month.toString() + "ヶ月"
		}
		if (day > 0) {
			uptime += day.toString() + "日 "
		}

		if (hour > 0) {
			uptime += hour.toString() + "時間"
		}
		if (minute > 0) {
			uptime += minute.toString() + "分"
		}
		if (second > 0) {
			uptime += second.toString() + "秒"
		}

		if (uptime.trim().isEmpty()) {
			uptime += "0秒"
		}

		return uptime
	}

	fun getLTime(time: Time): String {
		return time.toString()
	}

	fun getTimeFromDate(createdTime: Date): Time {
		val units = intArrayOf(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
			Calendar.SECOND)
		val today = Date()
		val result = IntArray(units.size)

		val sCal = Calendar.getInstance()
		val tCal = Calendar.getInstance()

		sCal.time = createdTime
		tCal.time = today

		for (i in units.indices.reversed()) {
			result[i] = tCal.get(units[i]) - sCal.get(units[i])
			if (result[i] < 0) {
				tCal.add(units[i - 1], -1)
				val add = tCal.getActualMaximum(units[i])
				result[i] += if (units[i] == Calendar.DAY_OF_MONTH) add else add + 1
			}
		}

		return Time(result[0], result[1], result[2], result[3], result[4], result[5])
	}

	data class Time(
		var year: Int = 0,
		var month: Int = 0,
		var day: Int = 0,
		var hour: Int = 0,
		var minute: Int = 0,
		var second: Int = 0
	) {

		override fun toString(): String {
			var uptime = ""

			if (hour > 0) {
				uptime += hour.toString() + "時間"
			}
			if (minute > 0) {
				uptime += minute.toString() + "分"
			}
			if (second > 0) {
				uptime += second.toString() + "秒"
			}

			if (uptime.trim().isEmpty()) {
				uptime += "0秒"
			}

			return uptime
		}

	}

	/**
	 * https://qiita.com/sifue/items/07388fdada096734fa7f
	 */
	fun postError(e: Exception) {
		val sw = StringWriter()
		val pw = PrintWriter(sw)
		e.printStackTrace(pw)
		pw.flush()
		val result = sw.toString()

		val file = this.stringToTempFile(result) ?: return

		for (id in this.getBotOwner().orEmpty()) {
			val user = this.jda.getUserById(id) ?: continue
			user.openPrivateChannel().complete().sendFiles(FileUpload.fromData(file)).complete()
		}
	}

	fun stringToTempFile(message: String): File? {
		try {
			val folder = File("temp")
			folder.mkdirs()

			val file = File(folder, UUID.randomUUID().toString().split("-")[0] + ".txt")

			Files.writeString(file.toPath(), message, StandardCharsets.UTF_8)
			return file
		} catch (e: IOException) {
			e.printStackTrace()
		}

		return null
	}

	fun getVideoId(url: String): String {
		var tempVideoId = url.replace("https://www.youtube.com/watch?v=", "")
		if (tempVideoId.contains("&")) {
			tempVideoId = tempVideoId.split("&")[0]
		}
		tempVideoId = tempVideoId.replace("https://youtu.be/", "")
		tempVideoId = tempVideoId.replace("http://youtu.be/", "")
		tempVideoId = tempVideoId.replace("http://music.youtube.com/watch?v=", "")
		tempVideoId = tempVideoId.replace("http://www.youtube.com/watch?v=", "")
		return tempVideoId
	}

	fun getBotOwner(): List<String>? {
		return this.config.getStringList("BotOwnerList")
	}

	fun isBotOwner(user: User): Boolean {
		var bool = false

		val botOwnerList = this.getBotOwner()
		if (botOwnerList.isNullOrEmpty()) {
			this.config.set("BotOwnerList", Arrays.asList("999", "888"))
			if (!this.notice) {
				println("Admin-ID が設定されていません。Config ファイルから Admin-ID を設定してください。")
				this.notice = true
			}
		} else {
			for (id in botOwnerList) {
				if (user.id == id) {
					bool = true
				}
			}
		}

		return bool
	}

	@Synchronized
	fun getGuildAudioPlayer(guild: Guild): GuildMusicManager {
		val guildId = guild.idLong
		var musicManager = this.musicManagers[guildId]

		if (musicManager == null) {
			musicManager = GuildMusicManager(this, guild, this.playerManager)
			this.musicManagers[guildId] = musicManager
		}

		guild.audioManager.sendingHandler = musicManager.sendHandler

		return musicManager
	}

	fun play(guild: Guild, musicManager: GuildMusicManager, track: AudioTrack) {
		val volume = this.config.getInt("Guild." + guild.id + ".Voice-Volume", 1)
		musicManager.player.volume = volume

		if (musicManager.player.isPaused) {
			musicManager.player.isPaused = false

			val nowPlaying = musicManager.player.playingTrack
			if (nowPlaying != null) {
				musicManager.scheduler.updateVoiceStatus(nowPlaying, TrackScheduler.TrackStatus.PLAY)
			}
		}

		musicManager.scheduler.queue(track)
	}

	@Suppress("UNUSED_PARAMETER")
	fun skipTrack(guild: Guild, channel: MessageChannel) {
		val musicManager = this.getGuildAudioPlayer(guild)
		musicManager.scheduler.nextTrack()

		// channel.sendMessage("次の曲にスキップします。").queue();
	}

	fun durationToTime(duration: String): Int {
		var i = 0

		try {
			if (duration.contains(":")) {
				val min = duration.split(":")[0].toInt()
				val sec = duration.split(":")[1].toInt()

				i += min * 60
				i += sec
			} else {
				i += duration.toInt()
			}
		} catch (e: Exception) {
		}

		return i
	}

	/**
	 * https://stackoverflow.com/questions/9214786/how-to-convert-the-seconds-in-this-format-hhmmss/47841277
	 */
	fun formatMillis(millis: Long): String {
		val secs = millis / 1000
		return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60)
	}

	fun formatSize(size: Double): String {
		val b = size
		val k = size / 1024.0
		val m = k / 1024.0
		val g = m / 1024.0
		val t = g / 1024.0

		val decimalFormat = DecimalFormat("0.00")

		return if (t > 1) {
			decimalFormat.format(t) + " TB"
		} else if (g > 1) {
			decimalFormat.format(g) + " GB"
		} else if (m > 1) {
			decimalFormat.format(m) + " MB"
		} else if (k > 1) {
			decimalFormat.format(k) + " KB"
		} else {
			decimalFormat.format(b) + " Bytes"
		}
	}

}
