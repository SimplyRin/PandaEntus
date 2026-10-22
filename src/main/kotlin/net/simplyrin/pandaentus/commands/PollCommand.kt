package net.simplyrin.pandaentus.commands

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
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
class PollCommand : BaseCommand() {

	companion object {

		// 投票アイテムの最大数 (スラッシュコマンド)
		private const val MAX_OPTIONS = 20

		// 投票として使用できるアイテムの最大数
		private const val MAX_ITEMS = 6

	}

	override val command = "!poll"

	override val description = "投票用プールメッセージを作成"

	override val commandData: CommandData
		get() {
			val commandData = CommandDataImpl("pool", this.description)
				.addOption(OptionType.STRING, "1", "1個目のアイテム", true)
				.addOption(OptionType.STRING, "2", "2個目のアイテム", true)

			for (i in 3..MAX_OPTIONS) {
				commandData.addOption(OptionType.STRING, i.toString(), "${i}個目のアイテム")
			}

			return commandData
		}

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		// 未指定のアイテムは null
		val slashCommandEvent = event.slashCommandEvent
		val arguments: List<String?> = if (slashCommandEvent != null) {
			listOf(this.command) + (1..MAX_OPTIONS).map { slashCommandEvent.getOption(it.toString())?.asString }
		} else {
			args.toList()
		}

		val user = event.author

		if (arguments.size > 1) {
			if (instance.isBotOwner(user) && arguments.size > 3 && arguments[1].equals("set", ignoreCase = true)) {
				val key = arguments[2].orEmpty()

				val game = arguments.drop(3).filterNotNull().joinToString(" ").trim()

				instance.poolItems.setItem(key, game)

				embedBuilder.setColor(Color.GREEN)
				embedBuilder.setDescription("`$key` を `$game` として覚えました。")

				event.reply(embedBuilder.build())
				return
			}

			var size = 0
			for (i in 1 until arguments.size) {
				if (i > MAX_ITEMS) {
					break
				}

				size = i

				val item = arguments[i] ?: continue

				embedBuilder.addField(i.toString(), instance.poolItems.getItem(item), true)
			}

			embedBuilder.setColor(Color.GREEN)
			embedBuilder.setDescription("投票が開始されました。")

			val message = event.reply(embedBuilder.build())

			for (integer in 1..size) {
				val value = instance.poolItems.getReaction(integer)
				println("Add: $value")

				message.addReaction(Emoji.fromFormatted(value)).complete()
			}
			return
		}

		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription("使用方法: " + arguments.firstOrNull().orEmpty() + " <1> <2> <3>... (最大: $MAX_ITEMS)")

		event.reply(embedBuilder.build())
	}

}
