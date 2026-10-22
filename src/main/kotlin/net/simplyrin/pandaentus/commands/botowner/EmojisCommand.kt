package net.simplyrin.pandaentus.commands.botowner

import java.io.IOException
import java.net.URI
import javax.net.ssl.HttpsURLConnection

import org.apache.commons.io.FilenameUtils
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.FileUpload
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.ThreadPool

/**
 * Created by SimplyRin on 2020/07/09.
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
class EmojisCommand : BaseCommand() {

	override val command = "!emojis"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val guild = event.guild
		val channel = event.channel

		if (args.size > 1) {
			if (args[1].equals("listguilds", ignoreCase = true)) {
				var message = "```\n"
				for (g in instance.jda.guilds) {
					message += g.id + " (" + g.name + ")\n"
				}
				channel.sendMessage(message + "```").complete()
				return
			}

			if (args[1].equals("list", ignoreCase = true)) {
				if (args.size > 2) {
					val g = instance.jda.getGuildById(args[2])
					if (g != null) {
						this.printEmojis(instance, g, channel)
						return
					}

					channel.sendMessage("Guild が見つかりませんでした。").complete()
					return
				}

				channel.sendMessage(this.command + " " + args[1] + " <guildId>").complete()
				return
			}

			if (args[1].equals("upload", ignoreCase = true)) {
				if (args.size > 4) {
					val g = instance.jda.getGuildById(args[2])
					if (g == null) {
						channel.sendMessage("Guild が見つかりませんでした。").complete()
						return
					}

					val emojiName = args[3]
					val emojiUrl = args[4]

					ThreadPool.run {
						try {
							val connection = URI(emojiUrl).toURL().openConnection() as HttpsURLConnection
							connection.addRequestProperty("user-agent", instance.botUserAgent)
							connection.connect()

							connection.inputStream.use { inputStream ->
								val extension = FilenameUtils.getExtension(emojiUrl)

								val icon = Icon.from(inputStream, Icon.IconType.fromExtension(extension))
								g.createEmoji(emojiName, icon).complete()
							}

							channel.sendMessage("絵文字をアップロードしました。").complete()
						} catch (e: Exception) {
							e.printStackTrace()
							channel.sendMessage("エラーが発生しました").complete()
						}
					}
					return
				}

				channel.sendMessage(this.command + " " + args[1] + " <guildId> <emojiName> <emojiUrl>").complete()
				return
			}
		}

		this.printEmojis(instance, guild, channel)
	}

	fun printEmojis(instance: PandaEntus, guild: Guild, channel: MessageChannel) {
		var message = ""
		for (emote in guild.emojis) {
			message += emote.name + " -> " + emote.id + ", " + emote.imageUrl + "\n"
		}

		if (message.length >= 2000) {
			val fm = message
			ThreadPool.run {
				val temp = instance.stringToTempFile(fm) ?: return@run

				channel.sendFiles(FileUpload.fromData(temp)).complete()

				try {
					Thread.sleep(5000)
				} catch (e: InterruptedException) {
					e.printStackTrace()
				}

				temp.delete()
			}
		} else {
			channel.sendMessage(message).complete()
		}
	}

}
