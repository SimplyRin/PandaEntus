package net.simplyrin.pandaentus.commands.botowner

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
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
class SendChatCommand : BaseCommand() {

	override val command = "!sendchat"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		if (args.size > 2) {
			val channel = instance.jda.textChannels.lastOrNull { it.id == args[1] }
			if (channel == null) {
				embedBuilder.setColor(Color.RED)
				embedBuilder.setDescription("チャンネルが見つかりませんでした。")
				event.channel.sendMessageEmbeds(embedBuilder.build()).complete()
				return
			}

			val message = args.drop(2).joinToString(" ").trim()

			channel.sendMessage(message).complete()
			return
		}

		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription("Usage: !sendchat <channelId> <message>")
		event.channel.sendMessageEmbeds(embedBuilder.build()).complete()
	}

}
