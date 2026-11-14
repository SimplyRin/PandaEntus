package net.simplyrin.pandaentus.tools

import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.tukaani.xz.XZInputStream

import net.simplyrin.config.Configuration
import net.simplyrin.pandaentus.utils.Platform
import net.simplyrin.pandaentus.utils.ProcessRunner

/**
 * Bot が使う外部ツール (yt-dlp と ffmpeg) を tools フォルダに用意して、ときどき更新する。
 *
 * - yt-dlp: https://github.com/yt-dlp/yt-dlp のリリース (Windows / Linux / macOS)
 * - ffmpeg: https://github.com/FFmpeg-Builds/FFmpeg-Builds のリリース (Windows / Linux。macOS 用はないので、PATH の ffmpeg を使う)
 *
 * config.yml の設定:
 * ```
 * Tools:
 *   AutoUpdate: true        # false にすると、ダウンロードも更新もしない
 *   YtDlp:
 *     Path: ''              # 自分で用意した yt-dlp を使うときに指定する
 *     UpdateIntervalDays: 7
 *   FFmpeg:
 *     Path: ''              # 自分で用意した ffmpeg (ファイル、またはフォルダ) を使うときに指定する
 *     UpdateIntervalDays: 30
 * ```
 *
 * ダウンロードしたファイルは、GitHub が返す SHA-256 と一致することを確認してから使う。
 */
class ExternalTools(
	private val config: Configuration,
	private val dataConfig: Configuration,
	private val saveData: () -> Unit
) {

	companion object {

		// ダウンロードしたツールを置くフォルダ
		val FOLDER = File("tools")

		private const val YT_DLP_RELEASE = "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest"
		private const val FFMPEG_RELEASE = "https://api.github.com/repos/FFmpeg-Builds/FFmpeg-Builds/releases/latest"

		// ダウンロード元として認める URL
		private const val DOWNLOAD_PREFIX = "https://github.com/"

		private const val USER_AGENT = "PandaEntus"

		private const val DAY_MILLIS = 24L * 60 * 60 * 1000

		// 展開するファイルの大きさの上限 (壊れたアーカイブでディスクを使い切らないようにする)
		private const val MAX_EXTRACT_SIZE = 1024L * 1024 * 1024

		private val FFMPEG_VERSIONED = Regex("""^ffmpeg-n(\d+)\.(\d+)-latest-(.+)-gpl-\d+\.\d+\.(zip|tar\.xz)$""")

	}

	private class Asset(val name: String, val url: String, val sha256: String?, val updatedAt: String)

	private val client: HttpClient = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(15))
		.followRedirects(HttpClient.Redirect.NORMAL)
		.build()

	private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
		Thread(runnable, "external-tools").apply { isDaemon = true }
	}

	/**
	 * 起動時と、その後は 1 日ごとに、更新が必要かどうかを確認する。
	 */
	fun start() {
		val config = this.config
		if (!config.contains("Tools.AutoUpdate")) {
			config.set("Tools.AutoUpdate", true)
		}

		this.scheduler.scheduleWithFixedDelay({
			try {
				this.checkUpdates()
			} catch (e: Exception) {
				println("[Tools] 更新の確認に失敗しました: $e")
			}
		}, 10, 24L * 60 * 60, TimeUnit.SECONDS)
	}

	/**
	 * 使用する yt-dlp のパス。見つからなければ null を返す。
	 */
	fun ytDlp(): String? {
		val configured = this.config.getString("Tools.YtDlp.Path", "").orEmpty()
		if (configured.isNotBlank()) {
			return configured.takeIf { File(it).isFile }
		}

		val managed = this.managedFile("yt-dlp")
		if (managed.isFile) {
			return managed.absolutePath
		}

		return Platform.findOnPath("yt-dlp")?.absolutePath
	}

	/**
	 * 使用する ffmpeg の場所 (ファイル、またはフォルダ)。null のときは、yt-dlp が PATH から探す。
	 */
	fun ffmpegLocation(): String? {
		val configured = this.config.getString("Tools.FFmpeg.Path", "").orEmpty()
		if (configured.isNotBlank()) {
			return configured.takeIf { File(it).exists() }
		}

		if (this.managedFile("ffmpeg").isFile) {
			return FOLDER.absolutePath
		}

		return null
	}

	/**
	 * yt-dlp を実行する。yt-dlp がない、または起動できなかったときは null を返す。
	 *
	 * URL などユーザーが入力した値は、オプションとして解釈されないように、必ず "--" の後ろに置くこと。
	 * 通信は config.yml の YouTube.Proxy (あれば) を経由する。
	 */
	fun runYtDlp(args: List<String>, timeoutSeconds: Long): ProcessRunner.Result? {
		val ytDlp = this.ytDlp()
		if (ytDlp == null) {
			println("[Tools] yt-dlp が見つかりません。")
			return null
		}

		// --ignore-config: サーバーに置かれた設定ファイルの影響を受けないようにする
		val command = mutableListOf(ytDlp, "--ignore-config", "--no-playlist", "--encoding", "utf-8")

		val config = this.config
		val proxyHost: String? = config.getString("YouTube.Proxy.Host", null)
		val proxyPort = config.getInt("YouTube.Proxy.Port", 0)
		if (!proxyHost.isNullOrEmpty() && proxyPort > 0) {
			command.add("--proxy")
			command.add("http://$proxyHost:$proxyPort")
		}

		val ffmpeg = this.ffmpegLocation()
		if (ffmpeg != null) {
			command.add("--ffmpeg-location")
			command.add(ffmpeg)
		}

		command.addAll(args)

		return ProcessRunner.run(command, timeoutSeconds)
	}

	private fun managedFile(name: String): File {
		return File(FOLDER, Platform.executable(name))
	}

	private fun checkUpdates() {
		synchronized(this) {
			val config = this.config
			if (!config.getBoolean("Tools.AutoUpdate", true)) {
				return
			}

			if (config.getString("Tools.YtDlp.Path", "").isNullOrBlank()) {
				try {
					this.updateYtDlp()
				} catch (e: Exception) {
					println("[Tools] yt-dlp の更新に失敗しました: $e")
				}
			}

			if (config.getString("Tools.FFmpeg.Path", "").isNullOrBlank()) {
				try {
					this.updateFfmpeg()
				} catch (e: Exception) {
					println("[Tools] ffmpeg の更新に失敗しました: $e")
				}
			}
		}
	}

	private fun isDue(checkedAtKey: String, intervalKey: String, defaultDays: Int): Boolean {
		val days = this.config.getInt(intervalKey, defaultDays).coerceAtLeast(1)
		val checkedAt = this.dataConfig.getLong(checkedAtKey, 0L)
		return System.currentTimeMillis() - checkedAt >= days * DAY_MILLIS
	}

	private fun saveState(versionKey: String, version: String, checkedAtKey: String) {
		val data = this.dataConfig
		synchronized(data) {
			data.set(versionKey, version)
			data.set(checkedAtKey, System.currentTimeMillis())
		}

		this.saveData()
	}

	private fun updateYtDlp() {
		val assetName = when {
			Platform.isWindows && Platform.isX64 -> "yt-dlp.exe"
			Platform.isWindows && Platform.isArm64 -> "yt-dlp_arm64.exe"
			Platform.isLinux && Platform.isX64 -> "yt-dlp_linux"
			Platform.isLinux && Platform.isArm64 -> "yt-dlp_linux_aarch64"
			Platform.isMac -> "yt-dlp_macos"
			else -> return
		}

		val target = this.managedFile("yt-dlp")
		if (target.isFile && !this.isDue("Tools.YtDlp.CheckedAt", "Tools.YtDlp.UpdateIntervalDays", 7)) {
			return
		}

		val release = this.getRelease(YT_DLP_RELEASE)
		val version = release.get("tag_name")?.asString.orEmpty()
		val asset = this.getAssets(release).firstOrNull { it.name == assetName }
			?: throw IOException("リリースに $assetName がありません。")

		if (target.isFile && version == this.dataConfig.getString("Tools.YtDlp.Version", "")) {
			this.saveState("Tools.YtDlp.Version", version, "Tools.YtDlp.CheckedAt")
			return
		}

		println("[Tools] yt-dlp $version をダウンロードしています...")

		FOLDER.mkdirs()
		val temp = File(FOLDER, "yt-dlp.download")
		try {
			this.download(asset, temp)
			this.install(temp, target)
		} finally {
			temp.delete()
		}

		this.saveState("Tools.YtDlp.Version", version, "Tools.YtDlp.CheckedAt")
		println("[Tools] yt-dlp を $version に更新しました。")
	}

	private fun updateFfmpeg() {
		val platform = when {
			Platform.isWindows && Platform.isX64 -> "win64"
			Platform.isWindows && Platform.isArm64 -> "winarm64"
			Platform.isLinux && Platform.isX64 -> "linux64"
			Platform.isLinux && Platform.isArm64 -> "linuxarm64"
			// macOS 用のビルドはない
			else -> return
		}

		val ffmpeg = this.managedFile("ffmpeg")
		val ffprobe = this.managedFile("ffprobe")
		val installed = ffmpeg.isFile && ffprobe.isFile
		if (installed && !this.isDue("Tools.FFmpeg.CheckedAt", "Tools.FFmpeg.UpdateIntervalDays", 30)) {
			return
		}

		val asset = this.selectFfmpegAsset(this.getAssets(this.getRelease(FFMPEG_RELEASE)), platform)
			?: throw IOException("リリースに $platform 用の ffmpeg がありません。")

		// 同じ名前のまま毎日ビルドし直されるので、更新日時も含めて比べる
		val version = asset.name + "@" + asset.updatedAt
		if (installed && version == this.dataConfig.getString("Tools.FFmpeg.Version", "")) {
			this.saveState("Tools.FFmpeg.Version", version, "Tools.FFmpeg.CheckedAt")
			return
		}

		println("[Tools] ffmpeg (" + asset.name + ") をダウンロードしています...")

		FOLDER.mkdirs()
		val archive = File(FOLDER, "ffmpeg.download")
		val newFfmpeg = File(FOLDER, "ffmpeg.new")
		val newFfprobe = File(FOLDER, "ffprobe.new")
		try {
			this.download(asset, archive)

			// アーカイブの中の bin/ffmpeg と bin/ffprobe だけを取り出す。出力先は固定なので、アーカイブ内のパスには左右されない
			val wanted = mapOf(
				"bin/" + Platform.executable("ffmpeg") to newFfmpeg,
				"bin/" + Platform.executable("ffprobe") to newFfprobe
			)
			val extracted = if (asset.name.endsWith(".zip")) {
				this.extractZip(archive, wanted)
			} else {
				this.extractTarXz(archive, wanted)
			}
			if (extracted != wanted.size) {
				throw IOException("アーカイブに ffmpeg / ffprobe が入っていません。")
			}

			this.install(newFfmpeg, ffmpeg)
			this.install(newFfprobe, ffprobe)
		} finally {
			archive.delete()
			newFfmpeg.delete()
			newFfprobe.delete()
		}

		this.saveState("Tools.FFmpeg.Version", version, "Tools.FFmpeg.CheckedAt")
		println("[Tools] ffmpeg を " + asset.name + " に更新しました。")
	}

	/**
	 * 安定版 (ffmpeg-n9.0-latest-... など) のうち一番新しいものを選ぶ。なければ master のビルドを選ぶ。
	 */
	private fun selectFfmpegAsset(assets: List<Asset>, platform: String): Asset? {
		val stable = assets
			.mapNotNull { asset ->
				val match = FFMPEG_VERSIONED.matchEntire(asset.name) ?: return@mapNotNull null
				if (match.groupValues[3] != platform) {
					return@mapNotNull null
				}

				val major = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
				val minor = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null
				Triple(major, minor, asset)
			}
			.maxWithOrNull(compareBy({ it.first }, { it.second }))
		if (stable != null) {
			return stable.third
		}

		return assets.firstOrNull {
			it.name == "ffmpeg-master-latest-$platform-gpl.zip" || it.name == "ffmpeg-master-latest-$platform-gpl.tar.xz"
		}
	}

	private fun getRelease(url: String): JsonObject {
		val request = HttpRequest.newBuilder(URI.create(url))
			.timeout(Duration.ofSeconds(30))
			.header("User-Agent", USER_AGENT)
			.header("Accept", "application/vnd.github+json")
			.GET()
			.build()

		val response = this.client.send(request, HttpResponse.BodyHandlers.ofString())
		if (response.statusCode() != 200) {
			throw IOException("GitHub API が HTTP " + response.statusCode() + " を返しました。")
		}

		return JsonParser.parseString(response.body()).asJsonObject
	}

	private fun getAssets(release: JsonObject): List<Asset> {
		val assets = release.getAsJsonArray("assets") ?: return emptyList()

		return assets.mapNotNull { element ->
			val asset = element.asJsonObject
			val name = asset.get("name")?.asString ?: return@mapNotNull null
			val url = asset.get("browser_download_url")?.asString ?: return@mapNotNull null
			val digest = asset.get("digest")?.takeIf { !it.isJsonNull }?.asString
			val sha256 = digest?.takeIf { it.startsWith("sha256:") }?.removePrefix("sha256:")

			Asset(name, url, sha256, asset.get("updated_at")?.asString.orEmpty())
		}
	}

	/**
	 * asset を output に保存する。SHA-256 が一致しなければ例外を投げる。
	 */
	private fun download(asset: Asset, output: File) {
		if (!asset.url.startsWith(DOWNLOAD_PREFIX)) {
			throw IOException("想定していないダウンロード元です: " + asset.url)
		}

		val expected = asset.sha256 ?: throw IOException(asset.name + " の SHA-256 を取得できませんでした。")

		val request = HttpRequest.newBuilder(URI.create(asset.url))
			.timeout(Duration.ofMinutes(30))
			.header("User-Agent", USER_AGENT)
			.GET()
			.build()

		val response = this.client.send(request, HttpResponse.BodyHandlers.ofInputStream())
		val digest = MessageDigest.getInstance("SHA-256")
		response.body().use { body ->
			if (response.statusCode() != 200) {
				throw IOException("ダウンロードに失敗しました。(HTTP " + response.statusCode() + ")")
			}

			DigestInputStream(body, digest).use { input ->
				Files.copy(input, output.toPath(), StandardCopyOption.REPLACE_EXISTING)
			}
		}

		val actual = HexFormat.of().formatHex(digest.digest())
		if (!actual.equals(expected, ignoreCase = true)) {
			output.delete()
			throw IOException(asset.name + " の SHA-256 が一致しません。")
		}
	}

	/**
	 * source を実行できる状態にして、target に置き換える。
	 * Windows では、target を実行している間は置き換えられない。そのときは例外になり、次の確認でやり直す。
	 */
	private fun install(source: File, target: File) {
		source.setExecutable(true, false)

		try {
			Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
		} catch (e: AtomicMoveNotSupportedException) {
			Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
		}
	}

	private fun extractZip(archive: File, wanted: Map<String, File>): Int {
		var extracted = 0

		ZipInputStream(BufferedInputStream(archive.inputStream())).use { zip ->
			while (true) {
				val entry = zip.nextEntry ?: break
				val output = if (entry.isDirectory) null else this.match(entry.name, wanted)
				if (output != null) {
					this.copyLimited(zip, output, MAX_EXTRACT_SIZE)
					extracted++
				}
			}
		}

		return extracted
	}

	/**
	 * tar.xz から必要なファイルを取り出す。tar は、512 バイトのヘッダとファイルの中身が交互に並ぶ形式。
	 */
	private fun extractTarXz(archive: File, wanted: Map<String, File>): Int {
		var extracted = 0

		XZInputStream(BufferedInputStream(archive.inputStream())).use { tar ->
			val header = ByteArray(512)

			while (tar.readNBytes(header, 0, header.size) == header.size) {
				// 中身がすべて 0 のヘッダは、アーカイブの終わり
				if (header.all { it == 0.toByte() }) {
					break
				}

				val size = this.tarString(header, 124, 12).trim().toLongOrNull(8)
					?: throw IOException("tar のヘッダを読み取れませんでした。")
				val type = header[156].toInt().toChar()

				var name = this.tarString(header, 0, 100)
				val prefix = this.tarString(header, 345, 155)
				if (prefix.isNotEmpty()) {
					name = "$prefix/$name"
				}

				// 通常のファイルは '0' または 0
				val output = if (type == '0' || type == '\u0000') this.match(name, wanted) else null

				var remaining = size
				if (output != null) {
					if (size > MAX_EXTRACT_SIZE) {
						throw IOException("ファイルが大きすぎます: $name")
					}

					Files.newOutputStream(output.toPath()).use { out ->
						val buffer = ByteArray(1 shl 16)
						while (remaining > 0) {
							val read = tar.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
							if (read < 0) {
								throw IOException("tar が途中で終わっています。")
							}

							out.write(buffer, 0, read)
							remaining -= read
						}
					}
					extracted++
				}

				// 残りと、512 バイト単位に揃えるための詰め物を読み飛ばす
				tar.skipNBytes(remaining + (512 - size % 512) % 512)
			}
		}

		return extracted
	}

	private fun tarString(header: ByteArray, offset: Int, length: Int): String {
		var end = offset
		while (end < offset + length && header[end] != 0.toByte()) {
			end++
		}

		return String(header, offset, end - offset, Charsets.UTF_8)
	}

	/**
	 * アーカイブ内のパス ("ffmpeg-n9.0-.../bin/ffmpeg" など) に対応する出力先を返す。
	 */
	private fun match(entryName: String, wanted: Map<String, File>): File? {
		val name = entryName.replace('\\', '/')
		return wanted.entries.firstOrNull { name == it.key || name.endsWith("/" + it.key) }?.value
	}

	private fun copyLimited(input: InputStream, output: File, limit: Long) {
		Files.newOutputStream(output.toPath()).use { out ->
			val buffer = ByteArray(1 shl 16)
			var total = 0L

			while (true) {
				val read = input.read(buffer)
				if (read < 0) {
					break
				}

				total += read
				if (total > limit) {
					throw IOException("ファイルが大きすぎます: " + output.name)
				}

				out.write(buffer, 0, read)
			}
		}
	}

}
