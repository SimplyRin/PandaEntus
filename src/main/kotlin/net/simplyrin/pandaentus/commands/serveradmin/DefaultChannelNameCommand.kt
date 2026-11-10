package net.simplyrin.pandaentus.commands.serveradmin

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

/**
 * Created by SimplyRin on 2022/06/30.
 *
 * Copyright (c) 2022 SimplyRin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class DefaultChannelNameCommand : BaseCommand() {

	override val command = "!defaultchannelname"

	override val aliases = listOf("!dcn")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.ServerAdministrator

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		if (args.size > 1) {
			if (args[1].equals("set", ignoreCase = true)) {
				if (args.size > 3) {
					val channelName = args[2]

					val defaultName = args.drop(3).joinToString(" ").trim()

					instance.config.set("DefaultChannelName.$channelName.Default", defaultName)
					instance.saveConfig()

					event.reply("$channelName -> \"$defaultName\"")
					return
				}

				event.reply("使用方法: " + this.command + " set <チャンネルID> <デフォルトチャンネル名>")
				return
			}

			if (args[1].equals("delete", ignoreCase = true)) {
				if (args.size > 2) {
					instance.config.set("DefaultChannelName." + args[2] + ".Default", null)

					event.reply("削除しました: " + args[2])
					return
				}

				event.reply("使用方法: " + this.command + " delete <チャンネルID>")
				return
			}
		}

		event.reply("使用方法: " + this.command + " <set|delete> <チャンネルID> <チャンネル名>")
	}

}
