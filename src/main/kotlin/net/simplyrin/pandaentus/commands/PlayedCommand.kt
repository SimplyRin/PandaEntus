package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.util.Calendar

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.tools.ImageColor

/**
 * Created by SimplyRin on 2022/04/03.
 *
 * Copyright (C) 2022 SimplyRin
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
class PlayedCommand : BaseCommand() {

	override val command = "!played"

	override val description = "プレイしているゲームのプレイ時間記録を管理"

	override val commandData: CommandData
		get() = CommandDataImpl("played", this.description)

	override val aliases = listOf("t!played")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val guild = event.guild
		val member = event.member

		val enabledPath = "Guild." + guild.id + "." + member.id + ".IsEnabledActivity"

		if (args.size > 1) {
			if (args[1].equals("enable", ignoreCase = true)) {
				instance.config.set(enabledPath, true)
				event.reply("ゲームプレイ記録を **有効** にしました。")
				return
			}

			if (args[1].equals("disable", ignoreCase = true)) {
				instance.config.set(enabledPath, false)
				event.reply("ゲームプレイ記録を **無効** にしました。")
				return
			}
		}

		val name = args.firstOrNull().orEmpty()

		val enabled = instance.config.getBoolean(enabledPath, false)

		// 記録表示
		if (enabled) {
			val baseKey = "Guild." + guild.id + "." + member.id + ".Game"

			val games = HashMap<String, Int>()
			for (gameName in instance.activityConfig.getSection(baseKey)?.keys.orEmpty()) {
				games[gameName] = instance.activityConfig.getInt("$baseKey.$gameName")
			}

			// プレイ時間が長い順に上位 5 件
			for ((game, played) in games.entries.sortedByDescending { it.value }.take(5)) {
				val calendar = Calendar.getInstance()
				calendar.add(Calendar.MINUTE, -played)

				val hours = played / 60
				embedBuilder.addField(game,
					instance.getUptime(calendar.time) + (if (hours >= 24) " (${hours}時間)" else ""), false)
			}

			val avatarUrl = member.user.effectiveAvatarUrl

			embedBuilder.setAuthor(member.effectiveName + " のゲーム記録", null, avatarUrl)

			val color = ImageColor.getColor(avatarUrl)
			if (color != null) {
				embedBuilder.setColor(color.asAwtColor)
			} else {
				embedBuilder.setColor(Color.WHITE)
			}

			embedBuilder.setFooter("'$name disable' で記録をオフにできます。")

			event.reply(embedBuilder)
			return
		}

		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription("あなたのゲームプレイ記録は現在無効に設定されています。有効にするには、`$name enable` と入力してください。")

		event.reply(embedBuilder)
	}

}
