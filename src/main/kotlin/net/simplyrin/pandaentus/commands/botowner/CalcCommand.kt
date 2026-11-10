package net.simplyrin.pandaentus.commands.botowner

import java.awt.Color
import java.util.Scanner

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
class CalcCommand : BaseCommand() {

	override val command = "="

	override val type = CommandType.StartsWith

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		val embedBuilder = EmbedBuilder()

		val input = args.firstOrNull().orEmpty().replace("=", "")
		if (input.isEmpty()) {
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("使用方法: =<計算式>\n=1+1")
			channel.sendMessageEmbeds(embedBuilder.build()).complete()
			return
		}

		val process = try {
			Runtime.getRuntime().exec(arrayOf("calc", input))
		} catch (e: Exception) {
			instance.postError(e)
			return
		}

		Scanner(process.inputStream).use { scanner ->
			if (scanner.hasNext()) {
				channel.sendMessage("結果: **" + scanner.nextLine().trim() + "**").complete()
			}
		}
	}

}
