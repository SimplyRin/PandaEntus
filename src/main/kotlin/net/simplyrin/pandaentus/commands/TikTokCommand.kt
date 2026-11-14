package net.simplyrin.pandaentus.commands

import java.io.File
import java.util.UUID

import net.dv8tion.jda.api.utils.FileUpload
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.SafeUrl
import net.simplyrin.pandaentus.utils.ThreadPool

/**
 * Created by SimplyRin on 2020/11/23.
 *
 * Copyright (C) 2020 SimplyRin
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
class TikTokCommand : BaseCommand() {

	override val command = "https://vt.tiktok.com/"

	override val type = CommandType.StartsWith

	override val permission = CommandPermission.Everyone

	/**
	 * TikTok の動画を yt-dlp でダウンロードして、Discord に送信する。通信は config.yml の YouTube.Proxy (あれば) を経由する。
	 */
	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val url = SafeUrl.parse(args.firstOrNull().orEmpty(), "tiktok.com") ?: return

		val guild = event.guild
		val id = UUID.randomUUID().toString().split("-")[0]

		ThreadPool.run {
			val folder = File("tiktok")
			folder.mkdirs()

			try {
				println("[TikTok] [$id] Downloading: $url")

				// Discord でそのまま再生できるように、H.264 の動画を優先する
				val result = instance.externalTools.runYtDlp(listOf(
					"-S", "vcodec:h264",
					"--output", File(folder, "$id.%(ext)s").absolutePath,
					"--", url), 300)

				val downloaded = folder.listFiles().orEmpty()
					.firstOrNull { it.isFile && it.name.startsWith("$id.") && !it.name.endsWith(".part") }

				if (result == null || !result.success || downloaded == null) {
					println("[TikTok] [$id] ダウンロードに失敗しました: " + (result?.describeFailure() ?: "yt-dlp を使用できません"))
					event.reply("TikTok の動画を取得できませんでした。")
					return@run
				}

				if (downloaded.length() > guild.maxFileSize) {
					event.reply("動画が大きすぎるため、送信できませんでした。(" + instance.formatSize(downloaded.length().toDouble()) + ")")
					return@run
				}

				// tiktok チャンネルがあればそちらへ、なければ実行したチャンネルへ送信する
				val tiktokChannel = guild.getTextChannelsByName("tiktok", true).firstOrNull()
				val channel = tiktokChannel ?: event.channel

				println("[TikTok] [$id] Sending to #" + channel.name)

				val upload = FileUpload.fromData(downloaded, "tiktok-$id." + downloaded.extension)
				val action = channel.sendFiles(upload)
				if (tiktokChannel != null) {
					action.setContent(event.author.asMention)
				}
				action.complete()
			} catch (e: Exception) {
				e.printStackTrace()
			} finally {
				// 途中で止まったときの .part なども含めて片付ける
				folder.listFiles().orEmpty()
					.filter { it.isFile && it.name.startsWith("$id.") }
					.forEach { it.delete() }
			}
		}
	}

}
