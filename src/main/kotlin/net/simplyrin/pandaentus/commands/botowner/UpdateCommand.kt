package net.simplyrin.pandaentus.commands.botowner

import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.Version

/**
 * Created by SimplyRin on 2021/11/21.
 *
 * Copyright (C) 2021 SimplyRin
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
class UpdateCommand : BaseCommand() {

	companion object {

		private const val REPOSITORY = "SimplyRin/PandaEntus"
		private const val RELEASES_URL = "https://github.com/$REPOSITORY/releases"
		private const val RELEASES_API = "https://api.github.com/repos/$REPOSITORY/releases?per_page=10"
		private const val USER_AGENT = "PandaEntus-Updater"

	}

	override val command = "!update"

	override val description = "PandaEntus のアップデート確認し、最新バージョンにアップデートします。"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		try {
			if (Version.GITHUB_RUN_NUMBER.isEmpty()) {
				event.reply("開発者ビルドを使用しているため、アップデートを確認できません。\n$RELEASES_URL")
				return
			}

			val release = this.getLatestRelease()
			if (release == null) {
				event.reply("アップデートの確認に失敗しました。")
				return
			}

			val runNumber = this.getRunNumber(release)
			val releaseUrl = release.get("html_url")?.asString.orEmpty()

			if (Version.GITHUB_RUN_NUMBER.equals(runNumber, ignoreCase = true)) {
				event.reply("最新の PandaEntus 🐼 を利用しています。\n$releaseUrl")
				return
			}

			val jarName = this.getJarName()
			val downloadUrl = this.getAssetUrl(release, jarName)
			if (downloadUrl == null) {
				event.reply("最新のリリースに $jarName が添付されていませんでした。\n$releaseUrl")
				return
			}

			event.reply("アップデートを確認しました。PandaEntus 🐼 を更新しています...。\n" +
				"現在のバージョン: **" + Version.GITHUB_RUN_NUMBER + "**\n" +
				"最新のバージョン: **" + runNumber + "**\n" +
				releaseUrl)

			val file = File("PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies-v" + runNumber + ".jar")

			val message = event.reply("ファイルをダウンロードしています...。")
			if (!this.downloadJar(downloadUrl, file)) {
				message.editMessage("ファイルのダウンロードに失敗しました。").complete()
				return
			}
			message.editMessage("ファイルをダウンロードしました。").complete()

			val now = File(jarName)
			if (now.delete()) {
				file.renameTo(now)
				message.editMessage("PandaEntus を更新しました。再起動してください。").complete()
			} else {
				message.editMessage("更新に失敗しました。Bot を停止して " + file.name + " を " + now.name + " に置き換えて使用してください。").complete()
			}
			return
		} catch (e: Exception) {
			e.printStackTrace()
		}

		event.reply("アップデートの確認に失敗しました。")
	}

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

	fun downloadJar(url: String, file: File): Boolean {
		try {
			val connection = URI.create(url).toURL().openConnection() as HttpURLConnection
			connection.addRequestProperty("user-agent", USER_AGENT)
			// browser_download_url は objects.githubusercontent.com へリダイレクトされる。
			connection.instanceFollowRedirects = true
			connection.connect()

			connection.inputStream.use { inputStream ->
				Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
			}
			return true
		} catch (e: Exception) {
			e.printStackTrace()
		}

		return false
	}

}
