package net.simplyrin.pandaentus.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
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
class DabCommand : BaseCommand() {

	override val command = "!dab"

	override val description = "<o/ ｲｲﾈ!"

	override val commandData: CommandData
		get() = CommandDataImpl("dab", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		ThreadPool.run {
			val asi = "\n   |\n  /\\"

			val frames = listOf("\\o>", "<o/")

			var message: Message? = null
			for (i in 0..5) {
				val current = message
				if (current == null) {
					message = event.reply("<o/$asi")
				} else {
					current.editMessage(frames[(i - 1) % frames.size] + asi).complete()
				}

				try {
					Thread.sleep(400)
				} catch (e: InterruptedException) {
				}
			}

			try {
				Thread.sleep(2500)
			} catch (e: InterruptedException) {
				e.printStackTrace()
			}

			event.message?.delete()?.complete()
			message?.delete()?.complete()
		}
	}

}
