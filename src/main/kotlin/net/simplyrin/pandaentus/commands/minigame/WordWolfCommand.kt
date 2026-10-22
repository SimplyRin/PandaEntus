package net.simplyrin.pandaentus.commands.minigame

import java.io.File

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.config.Config
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.gamemanager.wordwolf.WordWolfManager

/**
 * Created by SimplyRin on 2021/11/09.
 *
 * Copyright (C) 2021 SimplyRin
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
class WordWolfCommand : BaseCommand() {

	override val command = "!wordwolf"

	override val description = "ワードウルフゲームに接続"

	override val commandData: CommandData
		get() = CommandDataImpl("wordwolf", this.description)
			.addOption(OptionType.INTEGER, "人狼の人数", "人狼の人数を入力", true)
			.addOption(OptionType.STRING, "時間", "話し合いの時間を指定 (分:秒, HH:ss)", true)
			.addOption(OptionType.STRING, "テーマ", "選ばれるお題のテーマを指定 (入力しなければランダム)")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		val slashCommandEvent = event.slashCommandEvent
		val arguments = if (slashCommandEvent != null) {
			listOfNotNull(
				this.command,
				slashCommandEvent.getOption("人狼の人数")?.asString.orEmpty(),
				slashCommandEvent.getOption("時間")?.asString.orEmpty(),
				slashCommandEvent.getOption("テーマ")?.asString
			).toTypedArray()
		} else {
			args
		}

		if (arguments.size > 1) {
			if (arguments[1].equals("start", ignoreCase = true)) {
				val wwm = WordWolfManager.getGameByChannel(channel)
				if (wwm != null) {
					if (wwm.start() == null) {
						event.reply("ゲームは既に開始されています。")
					}
					return
				}

				event.reply("このチャンネルで開始を予定しているゲームを見つけることができませんでした。\n" + this.command + " コマンドを使用してゲームの開始を予約してください。")
				return
			}
		}

		if (arguments.size > 2) {
			val theme = arguments.getOrNull(3)

			val wolfs = arguments[1].toIntOrNull()
			val time = try {
				instance.mmssToSeconds(arguments[2])
			} catch (e: Exception) {
				null
			}
			if (wolfs == null || time == null) {
				event.reply("値は数字で入力してください。")
				return
			}

			if (WordWolfManager.getGameByChannel(channel) != null) {
				event.reply("既に開始されているゲームがあります。")
				return
			}

			val wwm = try {
				WordWolfManager(instance, event.guild, channel, wolfs, time, theme)
			} catch (e: IllegalArgumentException) {
				event.reply(e.message ?: "お題を取得できませんでした。")
				return
			}
			wwm.startRecruit(event)
			return
		}

		var theme = ""

		val folder = File("wordwolf")
		folder.mkdirs()

		for (file in folder.listFiles().orEmpty()) {
			val config = Config.getConfig(file)
			theme += config.getString("name") + "\n"
		}

		val name = arguments.firstOrNull().orEmpty()
		val reply = "$name <ワードウルフの人数> <時間 (分:秒)> <トークテーマ, 入力しなければランダム>\n" +
			"開始するには `$name start` と入力してください。\n" +
			"```テーマ:\n" +
			theme + "```"
		event.reply(reply)
	}

}
