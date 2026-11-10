package net.simplyrin.pandaentus.tools

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

/**
 * Created by SimplyRin on 2019/04/05.
 *
 * Copyright (C) 2019 SimplyRin
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
object EmbedMessage {

	@JvmStatic
	fun of(title: String?): MessageEmbed {
		return of(title, null)
	}

	@JvmStatic
	fun of(title: String?, message: String?): MessageEmbed {
		return of(title, message, null)
	}

	@JvmStatic
	fun of(title: String?, message: String?, author: String?): MessageEmbed {
		return of(title, message, author, null)
	}

	@JvmStatic
	fun of(title: String?, message: String?, author: String?, color: Color?, vararg fields: MessageEmbed.Field): MessageEmbed {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setTitle(title)
		embedBuilder.setDescription(message)
		embedBuilder.setAuthor(author)
		embedBuilder.setColor(color)
		for (field in fields) {
			embedBuilder.addField(field)
		}

		return embedBuilder.build()
	}

}
