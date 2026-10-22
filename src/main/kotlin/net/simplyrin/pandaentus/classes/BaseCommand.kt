package net.simplyrin.pandaentus.classes

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.simplyrin.pandaentus.PandaEntus

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
open class BaseCommand {

	open val command: String?
		get() = null

	open val description: String?
		get() = null

	open fun sendTyping(): Boolean {
		return true
	}

	open val commandData: CommandData?
		get() = null

	open val aliases: List<String>?
		get() = null

	open val type: CommandType?
		get() = null

	open val permission: CommandPermission?
		get() = null

	open fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
	}

}
