package net.simplyrin.pandaentus.commands.botowner

import java.io.File

import kotlin.system.exitProcess

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.Platform

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

	companion object {

		/**
		 * 再起動用のスクリプトの既定の名前 (Windows は start.bat、それ以外は start.sh)
		 */
		@JvmStatic
		fun defaultScript(): String {
			return if (Platform.isWindows) "start.bat" else "start.sh"
		}

	}

	override val command = "!restart"

	override val description = "Bot を再起動します。"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		if (args.size > 1) {
			if (args[1].equals("now", ignoreCase = true)) {
				val brs: String = instance.config.getString("Bot.Restart-Script") ?: defaultScript()

				val script = File(brs).absoluteFile
				if (!script.isFile) {
					event.reply("再起動用のスクリプト (" + brs + ") が見つかりません。config.yml の Bot.Restart-Script を確認してください。")
					return
				}

				event.reply("再起動しています...。")

				Runtime.getRuntime().addShutdownHook(Thread {
					try {
						val command = if (Platform.isWindows) {
							// start は、引用符で囲んだ最初の引数をウィンドウのタイトルとして扱う。
							// タイトルを明示しておかないと、空白を含むパスがタイトルとして解釈されてしまう
							listOf("cmd", "/c", "start", "\"PandaEntus\"", script.path)
						} else {
							listOf(if (File("/bin/bash").exists()) "/bin/bash" else "sh", script.path)
						}

						println("Restarting with: " + command.joinToString(" "))

						// 終了を待たずに起動する。出力は受け取らないので、捨てる
						ProcessBuilder(command)
							.redirectOutput(ProcessBuilder.Redirect.DISCARD)
							.redirectError(ProcessBuilder.Redirect.DISCARD)
							.start()
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
