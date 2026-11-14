package net.simplyrin.pandaentus.commands.botowner

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import oshi.SystemInfo

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

		// OS のコマンド (free) は使わず、OSHI で取得する (Windows / macOS / Linux で同じ動きになる)
		val memory = try {
			SystemInfo().hardware.memory
		} catch (e: Throwable) {
			println("[Free] メモリの情報を取得できませんでした: $e")
			channel.sendMessage("メモリの情報を取得できませんでした。").complete()
			return
		}

		val total = memory.total
		// 空き = すぐに使える量 (キャッシュなど、解放できる分を含む)
		val free = memory.available
		val used = total - free

		val swapTotal = memory.virtualMemory.swapTotal
		val swapUsed = memory.virtualMemory.swapUsed
		val swapFree = swapTotal - swapUsed

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.YELLOW)
		embedBuilder.setAuthor("Ram usage")
		embedBuilder.addField("Total", instance.formatSize(total.toDouble()), true)
		embedBuilder.addField("Used", instance.formatSize(used.toDouble()), true)
		embedBuilder.addField("Free", instance.formatSize(free.toDouble()), true)
		embedBuilder.addField("Swap Total", instance.formatSize(swapTotal.toDouble()), true)
		embedBuilder.addField("Swap Used", instance.formatSize(swapUsed.toDouble()), true)
		embedBuilder.addField("Swap Free", instance.formatSize(swapFree.toDouble()), true)

		channel.sendMessageEmbeds(embedBuilder.build()).complete()
	}

}
