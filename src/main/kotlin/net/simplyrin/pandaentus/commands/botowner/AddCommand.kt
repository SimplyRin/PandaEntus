package net.simplyrin.pandaentus.commands.botowner

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

/**
 * Created by SimplyRin on 2020/10/20.
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
class AddCommand : BaseCommand() {

	override val command = "!add"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		if (args.size > 3) {
			val config = instance.config
			config.set("LocalMessage." + channel.id + ".Message", args[1])
			config.set("LocalMessage." + channel.id + ".Value.1", args[2])
			config.set("LocalMessage." + channel.id + ".Value.2", args[3])

			channel.sendMessage("設定しました。").complete()
			return
		}

		channel.sendMessage("使用方法: !add <メッセージ> <内容1> <内容2>").complete()
	}

}
