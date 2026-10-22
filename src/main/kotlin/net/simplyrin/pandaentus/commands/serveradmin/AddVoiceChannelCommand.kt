package net.simplyrin.pandaentus.commands.serveradmin

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
class AddVoiceChannelCommand : BaseCommand() {

	override val command = "!add-vc"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.ServerAdministrator

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val channel = event.channel

		val category = instance.getVoiceChannelCategory(event.guild)
		if (category == null) {
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("Voice Channels カテゴリが見つかりません。")
			channel.sendMessageEmbeds(embedBuilder.build()).complete()
			return
		}

		if (args.size > 1) {
			val i = args[1].toIntOrNull()
			if (i == null) {
				embedBuilder.setColor(Color.RED)
				embedBuilder.setDescription("Invalid usage!")
				channel.sendMessageEmbeds(embedBuilder.build()).complete()
				return
			}

			var size = category.voiceChannels.size + 1
			var count = 0
			while (i != count) {
				try {
					category.createVoiceChannel("General-$size").setUserlimit(99).complete()
				} catch (e: Exception) {
				}

				if (size == 50) {
					break
				}

				size++
				count++
			}

			embedBuilder.setColor(Color.GREEN)
			embedBuilder.setDescription("Created!")
			channel.sendMessageEmbeds(embedBuilder.build()).complete()
		}
	}

}
