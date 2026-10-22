package net.simplyrin.pandaentus.commands

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
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
class VanishCommand : BaseCommand() {

	override val command = "!vanish"

	override val description = "通話ログ一覧から自分を表示しないように"

	override val commandData: CommandData
		get() = CommandDataImpl("vanish", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		var user = event.author

		// Bot オーナーは他のユーザーを指定できる
		if (args.size > 1 && instance.isBotOwner(user)) {
			val id = args[1].replace("<", "").replace(">", "").replace("!", "").replace("@", "")

			val member = event.guild.getMemberById(id)
			if (member != null) {
				user = member.user
			}
		}

		val config = instance.config

		val path = "User." + user.id + "." + event.guild.id + ".Vanish"

		val bool = !config.getBoolean(path)
		config.set(path, bool)

		embedBuilder.setColor(Color.GRAY)
		embedBuilder.setDescription("通話ログ表示: " + (if (bool) "オフ" else "オン") + ".")

		event.reply(embedBuilder.build())
	}

}
