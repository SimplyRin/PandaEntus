package net.simplyrin.pandaentus.commands

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

/**
 * Created by SimplyRin on 2020/07/17.
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
class IconCommand : BaseCommand() {

	override val command = "!icon"

	override val description = "アカウントのアイコンを表示"

	override val commandData: CommandData
		get() = CommandDataImpl("icon", this.description)
			.addOption(OptionType.USER, "ユーザー", "指定したユーザーのアイコンを表示", true)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		val arguments = if (event.isSlashCommand()) {
			arrayOf(this.command, event.slashCommandEvent?.getOption("ユーザー")?.asString ?: event.user.id)
		} else {
			args
		}

		if (arguments.size > 1) {
			val id = arguments[1].replace("@", "").replace("<", "").replace(">", "").replace("!", "")

			val member = event.guild.getMemberById(id)
			if (member != null) {
				channel.sendMessage(member.user.effectiveAvatarUrl).complete()
			} else {
				channel.sendMessage("ユーザーが見つかりませんでした。").complete()
			}
			return
		}

		event.reply("使用方法: !icon <ユーザーID|メンション>")
	}

}
