package net.simplyrin.pandaentus.tools;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

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
public class EmbedMessage {

	public static MessageEmbed of(String title) {
		return of(title, null);
	}

	public static MessageEmbed of(String title, String message) {
		return of(title, message, null);
	}

	public static MessageEmbed of(String title, String message, String author) {
		return of(title, message, author);
	}

	public static MessageEmbed of(String title, String message, String author, Color color) {
		return of(title, message, author, color, (MessageEmbed.Field[]) null);
	}

	public static MessageEmbed of(String title, String message, String author, Color color, MessageEmbed.Field... fields) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle(title);
		embedBuilder.setDescription(message);
		embedBuilder.setAuthor(author);
		embedBuilder.setColor(color);
		if (fields != null) {
			for (MessageEmbed.Field field : fields) {
				embedBuilder.addField(field);
			}
		}

		return embedBuilder.build();
	}

}
