package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.util.Date

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

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
class UptimeCommand : BaseCommand() {

	override val command = "!uptime"

	override val description = "Bot の起動時間を確認"

	override val commandData: CommandData
		get() = CommandDataImpl("uptime", this.description)
			.addOption(OptionType.USER, "ユーザー", "指定したユーザーの通話時間を確認")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val guild = event.guild
		val user = event.author

		if (instance.config.getBoolean("Disable")) {
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("この機能は現在一時的に無効にされています。")

			event.reply(embedBuilder.build())
			return
		}

		val arguments = if (event.isSlashCommand()) {
			arrayOf(this.command, event.slashCommandEvent?.getOption("ユーザー")?.asString ?: event.user.id)
		} else {
			args
		}

		if (arguments.size > 1) {
			var id = arguments[1].replace("<", "").replace(">", "").replace("@", "").replace("!", "")
			if (id.toLongOrNull() == null) {
				id = user.id
			}

			val inVoiceChannel = guild.voiceChannels.any { voiceChannel -> voiceChannel.members.any { it.id == id } }
			if (!inVoiceChannel) {
				embedBuilder.setColor(Color.RED)
				embedBuilder.setDescription("このユーザーは現在通話していません。")

				event.reply(embedBuilder.build())
				return
			}

			val timeManager = instance.timeManager.getUser(id)
			if (!timeManager.isJoined) {
				embedBuilder.setColor(Color.RED)
				embedBuilder.setDescription("このユーザーは現在通話していません。")

				event.reply(embedBuilder.build())
				return
			}

			embedBuilder.setColor(Color.GREEN)
			embedBuilder.setAuthor(event.member.effectiveName, user.avatarUrl, user.avatarUrl)
			embedBuilder.addField("参加時間", timeManager.getJoinedTime(), true)
			embedBuilder.addField("通話時間", timeManager.getCurrentTime(), true)

			event.reply(embedBuilder.build())
			return
		}

		val channels = instance.getVoiceChannelCategory(guild)?.channels.orEmpty()
		if (channels.size <= 1) {
			embedBuilder.setDescription("現在通話していません。")
			embedBuilder.setColor(Color.GREEN)

			event.reply(embedBuilder.build())
			return
		}

		// General-2 チャンネルを取得
		val guildChannel = channels[1]
		val time = Date.from(guildChannel.timeCreated.toInstant())

		embedBuilder.setColor(Color.GREEN)
		embedBuilder.addField("グループ合計通話時間", instance.getUptime(time), false)

		event.reply(embedBuilder.build())
	}

}
