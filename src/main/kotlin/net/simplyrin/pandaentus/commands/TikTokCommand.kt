package net.simplyrin.pandaentus.commands

import java.io.File
import java.util.concurrent.TimeUnit

import com.google.gson.JsonParser
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.ThreadPool
import net.simplyrin.processmanager.Callback
import net.simplyrin.processmanager.ProcessManager

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

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val file = File(File("tiktok"), "api.php")

		val content = event.message?.contentRaw ?: return

		ProcessManager.runCommand(arrayOf("/usr/bin/php", file.absolutePath, content), object : Callback() {

			override fun line(response: String?) {
				if (response == null) {
					return
				}

				println("TikTokCommand.java: $response")

				// tiktok チャンネルがあればそちらへ、なければ実行したチャンネルへ送信する
				val tiktokChannel = event.guild.getTextChannelsByName("tiktok", true).firstOrNull()
				val channel = tiktokChannel ?: event.channel
				val mention = tiktokChannel != null

				println("TikTokCommand.java: TikTok URL sending to #" + channel.name)

				val jsonObject = try {
					JsonParser.parseString(response).asJsonObject
				} catch (e: Exception) {
					return
				}

				val url = jsonObject.get("url")?.asString ?: return
				val path = jsonObject.get("path")?.asString ?: return

				val message = channel.sendMessage((if (mention) event.author.asMention + " " else "") + url).complete()

				val downloaded = File(path)
				downloaded.deleteOnExit()

				ThreadPool.run {
					try {
						TimeUnit.DAYS.sleep(1)
					} catch (e: Exception) {
					}

					println("Delete: $path")
					downloaded.delete()
					message.delete().complete()
				}
			}

		}, true)
	}

}
