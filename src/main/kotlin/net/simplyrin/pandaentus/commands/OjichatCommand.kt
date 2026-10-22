package net.simplyrin.pandaentus.commands

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.ojichat.generator.OjichatConfig
import net.simplyrin.ojichat.generator.OjichatGenerator
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
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
class OjichatCommand : BaseCommand() {

	override val command = "!ojichat"

	override val description = "おじさん構文を生成"

	override val commandData: CommandData
		get() = CommandDataImpl("ojichat", this.description)
			.addOption(OptionType.STRING, "名前", "指定した名前でおじさん構文を生成します。")
			.addOption(OptionType.NUMBER, "絵文字数", "絵文字数を指定。デフォルト: 4")
			.addOption(OptionType.NUMBER, "句読点レベル", "句読点レベルを指定。デフォルト: 0")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val slashCommandEvent = event.slashCommandEvent
		val arguments = if (slashCommandEvent != null) {
			arrayOf(
				this.command,
				slashCommandEvent.getOption("名前")?.asString.orEmpty(),
				(slashCommandEvent.getOption("絵文字数")?.asInt ?: 4).toString(),
				(slashCommandEvent.getOption("句読点レベル")?.asInt ?: 0).toString()
			)
		} else {
			args
		}

		val channel = event.channel

		var name = arguments.drop(1).joinToString(" ").trim()

		var normalText = false
		if (name.contains("-normal") || name.contains("-n")) {
			normalText = true
			name = name.replace("-normal", "")
			name = name.replace("-n", "")
		}

		val result = try {
			val config = OjichatConfig()
			config.setTargetName(name) // 名前を指定
			config.setEmojiNum(arguments.getOrNull(2)?.toIntOrNull() ?: 4) // 絵文字数を指定
			config.setPunctuationLevel(arguments.getOrNull(3)?.toIntOrNull() ?: 0) // 句読点レベルを指定
			OjichatGenerator.start(config)
		} catch (e: Exception) {
			return
		}

		embedBuilder.setColor(Color.GREEN)
		embedBuilder.setDescription(result)

		if (event.isSlashCommand()) {
			event.reply(embedBuilder.build())
		} else {
			if (normalText) {
				channel.sendMessage(result).complete()
			} else {
				channel.sendMessageEmbeds(embedBuilder.build()).complete()
			}
		}
	}

}
