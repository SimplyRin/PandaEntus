package net.simplyrin.pandaentus.commands.serveradmin

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

/**
 * Created by SimplyRin on 2022/02/09.
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
class VoiceTextChannelCommand : BaseCommand() {

	override val command = "!voicetextchannel"

	override val aliases = listOf("!vtc")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.ServerAdministrator

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val config = instance.config

		val path = "Server." + event.guild.id + ".EnableVoiceTextChannel"

		val bool = !config.getBoolean(path, false)
		config.set(path, bool)

		event.reply("通話者用通話チャンネルを **" + (if (bool) "無効" else "有効") + "** にしました。")
	}

}
