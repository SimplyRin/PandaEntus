package net.simplyrin.pandaentus.commands.botowner

import kotlin.system.exitProcess

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.processmanager.Callback
import net.simplyrin.processmanager.ProcessManager

/**
 * Created by SimplyRin on 2021/11/21.
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
class RestartCommand : BaseCommand() {

	override val command = "!restart"

	override val description = "Bot を再起動します。"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		if (args.size > 1) {
			if (args[1].equals("now", ignoreCase = true)) {
				event.reply("再起動しています...。")

				val brs: String = instance.config.getString("Bot.Restart-Script") ?: "start.sh"

				Runtime.getRuntime().addShutdownHook(Thread {
					try {
						val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")

						println("Restarting with $brs isWindows: $isWindows")
						val cmd = (if (isWindows) "cmd /c start " else "/bin/sh ") + brs
						println("Command: $cmd")

						val command = if (isWindows) {
							arrayOf("cmd", "/c", "start", brs)
						} else {
							arrayOf("/bin/bash", brs)
						}

						ProcessManager.runCommand(command, object : Callback() {

							override fun line(line: String?) {
								println(line)
							}

							override fun processEnded() {
								println("Process ended.")
							}

						}, false)
					} catch (e: Exception) {
						e.printStackTrace()
					}
				})

				exitProcess(0)
			}

			if (args[1].equals("inactive", ignoreCase = true)) {
				event.reply("この機能はまだ実装されていません。")
				return
			}
		}

		event.reply("使用方法: /restart <now|inactive>\n" +
			"- now: Bot を今すぐ再起動します。\n" +
			"- inactive: 通話が進行している場合、全ての通話が終了した時 Bot を再起動します。")
	}

}
