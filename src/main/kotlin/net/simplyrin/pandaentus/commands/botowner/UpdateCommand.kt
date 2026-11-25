package net.simplyrin.pandaentus.commands.botowner

import java.io.File
import java.nio.file.Files

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.tools.Updater
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

	override val command = "!update"

	override val description = "PandaEntus のアップデート確認し、最新バージョンにアップデートします。"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		try {
			if (Version.GITHUB_RUN_NUMBER.isEmpty()) {
				event.reply("開発者ビルドを使用しているため、アップデートを確認できません。\n${Updater.RELEASES_URL}")
				return
			}

			val release = Updater.getLatestRelease()
			if (release == null) {
				event.reply("アップデートの確認に失敗しました。")
				return
			}

			val runNumber = Updater.getRunNumber(release)
			val releaseUrl = release.get("html_url")?.asString.orEmpty()

			if (Version.GITHUB_RUN_NUMBER.equals(runNumber, ignoreCase = true)) {
				event.reply("最新の PandaEntus 🐼 を利用しています。\n$releaseUrl")
				return
			}

			val jarName = Updater.getJarName()
			val downloadUrl = Updater.getAssetUrl(release, jarName)
			if (downloadUrl == null) {
				event.reply("最新のリリースに $jarName が添付されていませんでした。\n$releaseUrl")
				return
			}

			event.reply("アップデートを確認しました。PandaEntus 🐼 を更新しています...。\n" +
				"現在のバージョン: **" + Version.GITHUB_RUN_NUMBER + "**\n" +
				"最新のバージョン: **" + runNumber + "**\n" +
				releaseUrl)

			// 実行中の jar と同じフォルダに保存する
			val now = Updater.getRunningJar() ?: File(jarName).absoluteFile
			val file = File(now.parentFile, "PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies-v" + runNumber + ".jar")

			val message = event.reply("ファイルをダウンロードしています...。")
			if (!Updater.downloadJar(downloadUrl, file, Updater.getAssetSha256(release, jarName))) {
				file.delete()
				message.editMessage("ファイルのダウンロードに失敗しました。").complete()
				return
			}
			message.editMessage("ファイルをダウンロードしました。").complete()

			val pending = Updater.install(file, now)
			if (pending == null) {
				// Linux / macOS では、実行中の jar をそのまま置き換えられる
				message.editMessage("PandaEntus を更新しました。再起動してください。").complete()
			} else {
				// Windows では、実行中の jar を置き換えられない。別名で残して、次の起動のときに start.bat が置き換える
				message.editMessage("新しいバージョンを " + pending.name + " に保存しました。\n" +
					"同梱の start.bat で再起動すると、自動で置き換わります。" +
					"自分で置き換える場合は、Bot を停止してから " + pending.name + " を " + now.name + " に上書きしてください。").complete()
			}
			return
		} catch (e: Exception) {
			e.printStackTrace()
		}

		event.reply("アップデートの確認に失敗しました。")
	}

}
