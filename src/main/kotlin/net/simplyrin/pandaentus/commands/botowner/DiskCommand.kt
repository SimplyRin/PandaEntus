package net.simplyrin.pandaentus.commands.botowner

import java.awt.Color
import java.io.File

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
class DiskCommand : BaseCommand() {

	override val command = "!disk"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		val file = File(".")
		val totalSpace = file.totalSpace
		val usableSpace = file.usableSpace
		val usedSpace = totalSpace - usableSpace

		val percent = if (totalSpace > 0) (usedSpace * 100 / totalSpace).toInt() else 0

		val color = when {
			percent >= 80 -> Color.RED
			percent >= 60 -> Color.YELLOW
			else -> Color.GREEN
		}

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(color)
		embedBuilder.setAuthor("Disk usage ($percent/100%)")
		embedBuilder.addField("Size", instance.formatSize(totalSpace.toDouble()), true)
		embedBuilder.addField("Used", instance.formatSize(usedSpace.toDouble()), true)
		embedBuilder.addField("Free", instance.formatSize(usableSpace.toDouble()), true)

		channel.sendMessageEmbeds(embedBuilder.build()).complete()
	}

}
