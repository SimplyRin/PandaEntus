package net.simplyrin.pandaentus.tools

import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.HexFormat

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.utils.Version

/**
 * PandaEntus 自身のアップデート (GitHub の Pre-release から jar を取得する)。
 * `!update` と、深夜の自動アップデート ([AutoUpdater]) の両方から使う。
 */
object Updater {

	const val REPOSITORY = "SimplyRin/PandaEntus"
	const val RELEASES_URL = "https://github.com/$REPOSITORY/releases"
	private const val RELEASES_API = "https://api.github.com/repos/$REPOSITORY/releases?per_page=10"
	private const val USER_AGENT = "PandaEntus-Updater"

	// Windows で、次の起動のときに置き換える jar に付ける拡張子 (start.bat が参照する)
	const val UPDATE_SUFFIX = ".update"

	fun getJarName(): String {
		return "PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies.jar"
	}

	/**
	 * Pre-release も含めた最新のリリースを返します。draft は対象外です。
	 */
	fun getLatestRelease(): JsonObject? {
		try {
			val connection = URI.create(RELEASES_API).toURL().openConnection() as HttpURLConnection
			connection.addRequestProperty("user-agent", USER_AGENT)
			connection.addRequestProperty("accept", "application/vnd.github+json")
			connection.connect()

			val releases = InputStreamReader(connection.inputStream, StandardCharsets.UTF_8).use { reader ->
				JsonParser.parseReader(reader).asJsonArray
			}

			// リリース一覧は作成日時の新しい順に返るため、draft でない最初の要素が最新。
			for (element in releases) {
				val release = element.asJsonObject
				if (release.get("draft")?.asBoolean != true) {
					return release
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return null
	}

	/**
	 * タグ名 (v123) から GitHub Actions の run number (123) を取り出します。
	 */
	fun getRunNumber(release: JsonObject): String {
		val tagName = release.get("tag_name")?.asString.orEmpty()
		return tagName.removePrefix("v")
	}

	fun getAssetUrl(release: JsonObject, assetName: String): String? {
		val assets = release.getAsJsonArray("assets") ?: return null

		for (element in assets) {
			val asset = element.asJsonObject
			if (asset.get("name")?.asString == assetName) {
				return asset.get("browser_download_url")?.asString
			}
		}

		return null
	}

	/**
	 * GitHub が返す、添付ファイルの SHA-256。古いリリースなどで取得できなければ null を返す。
	 */
	fun getAssetSha256(release: JsonObject, assetName: String): String? {
		val assets = release.getAsJsonArray("assets") ?: return null

		for (element in assets) {
			val asset = element.asJsonObject
			if (asset.get("name")?.asString == assetName) {
				val digest = asset.get("digest")?.takeIf { !it.isJsonNull }?.asString ?: return null
				return digest.takeIf { it.startsWith("sha256:") }?.removePrefix("sha256:")
			}
		}

		return null
	}

	/**
	 * 実行中の jar ファイル。jar から起動していない (開発中など) 場合は null を返す。
	 */
	fun getRunningJar(): File? {
		return try {
			val location = PandaEntus::class.java.protectionDomain?.codeSource?.location ?: return null
			File(location.toURI()).takeIf { it.isFile && it.name.endsWith(".jar") }
		} catch (e: Exception) {
			null
		}
	}

	/**
	 * url のファイルを file に保存する。sha256 が分かっているときは、一致することも確認する。
	 */
	fun downloadJar(url: String, file: File, sha256: String?): Boolean {
		try {
			if (!url.startsWith("https://github.com/$REPOSITORY/")) {
				println("[Update] 想定していないダウンロード元です: $url")
				return false
			}

			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.addRequestProperty("user-agent", USER_AGENT)
			// browser_download_url は objects.githubusercontent.com へリダイレクトされる。
			connection.instanceFollowRedirects = true
			connection.connect()

			val digest = MessageDigest.getInstance("SHA-256")
			DigestInputStream(connection.inputStream, digest).use { inputStream ->
				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
			}

			val actual = HexFormat.of().formatHex(digest.digest())
			if (sha256 != null && !actual.equals(sha256, ignoreCase = true)) {
				println("[Update] ダウンロードしたファイルの SHA-256 が一致しません。")
				return false
			}
			return true
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return false
	}

	/**
	 * ダウンロードした jar (file) を、実行中の jar (now) と置き換える。
	 * 置き換えられた (Linux / macOS) ときは null を返す。
	 * Windows では、実行中の jar を置き換えられない。そのときは別名で残して、そのファイルを返す。
	 */
	fun install(file: File, now: File): File? {
		try {
			Files.move(file.toPath(), now.toPath(), StandardCopyOption.REPLACE_EXISTING)
			return null
		} catch (e: IOException) {
			val pending = File(now.parentFile, now.name + UPDATE_SUFFIX)
			Files.move(file.toPath(), pending.toPath(), StandardCopyOption.REPLACE_EXISTING)
			return pending
		}
	}

}
