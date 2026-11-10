package net.simplyrin.pandaentus.commands.botowner

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.processmanager.Callback
import net.simplyrin.processmanager.ProcessManager

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
class FreeCommand : BaseCommand() {

	override val command = "!free"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		ProcessManager.runCommand(arrayOf("free", "-h"), object : Callback() {

			var memTotal = ""
			var memUsed = ""
			var memFree = ""
			var swapTotal = ""
			var swapUsed = ""
			var swapFree = ""

			override fun line(line: String?) {
				if (line == null) {
					return
				}

				println(line)

				if (line.startsWith("Mem:")) {
					val columns = this.parseColumns(line)

					this.memTotal = columns.getOrNull(1).orEmpty()
					this.memUsed = columns.getOrNull(2).orEmpty()
					this.memFree = columns.getOrNull(3).orEmpty()
				} else if (line.startsWith("Swap")) {
					val columns = this.parseColumns(line)

					this.swapTotal = columns.getOrNull(1).orEmpty()
					this.swapUsed = columns.getOrNull(2).orEmpty()
					this.swapFree = columns.getOrNull(3).orEmpty()
				}
			}

			private fun parseColumns(line: String): List<String> {
				val columns = line.trim().replace(Regex(" +"), " ").split(" ")
				for ((i, column) in columns.withIndex()) {
					println("args[$i] -> $column")
				}
				return columns
			}

			override fun processEnded() {
				try {
					Thread.sleep(100)
				} catch (e: Exception) {
				}

				val embedBuilder = EmbedBuilder()
				embedBuilder.setColor(Color.YELLOW)
				embedBuilder.setAuthor("Ram usage")
				embedBuilder.addField("Total", this.memTotal, true)
				embedBuilder.addField("Used", this.memUsed, true)
				embedBuilder.addField("Free", this.memFree, true)
				embedBuilder.addField("Swap Total", this.swapTotal, true)
				embedBuilder.addField("Swap Used", this.swapUsed, true)
				embedBuilder.addField("Swap Free", this.swapFree, true)

				channel.sendMessageEmbeds(embedBuilder.build()).complete()
			}

		}, true)
	}

}
