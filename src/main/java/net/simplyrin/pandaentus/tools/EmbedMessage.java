package net.simplyrin.pandaentus.tools;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Created by SimplyRin on 2019/04/05.
 *
 * Copyright (c) 2019 SimplyRin
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
