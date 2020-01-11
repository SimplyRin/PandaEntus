package net.simplyrin.pandaentus.listeners;

import java.awt.Color;

import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.utils.Version;

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
@AllArgsConstructor
public class PrivateChatMessage {

	private Main instance;

	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		MessageChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().split(" ");
		User user = event.getAuthor();

		EmbedBuilder embedBuilder = new EmbedBuilder();

		if (user.isBot() || user.isFake()) {
			return;
		}

		if (!user.getId().equals("224428706209202177")) {
			embedBuilder.setColor(Color.RED);
			embedBuilder.setDescription("You don't have access to this command");
			channel.sendMessage(embedBuilder.build()).complete();
			return;
		}

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("!help")) {
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("!help", "`Show this help message`", false);
				embedBuilder.addField("!toggle", "`Toggle this bot`", false);
				embedBuilder.addField("!simplemode", "`Display only talk time when call ends`", false);
				embedBuilder.addField("!version", "`Display currently running PandaEntus version`", false);

				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!toggle")) {
				boolean bool = this.instance.getConfig().getBoolean("Disable");
				this.instance.getConfig().set("Disable", !bool);

				embedBuilder.setColor(Color.GREEN);
				embedBuilder.setDescription("Disable call notification messages: `" + (!bool ? "Enabled" : "Disabled") + "`");

				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!simplemode")) {
				boolean bool = this.instance.getConfig().getBoolean("Message-Type.Enable-Simple-Mode");
				this.instance.getConfig().set("Message-Type.Enable-Simple-Mode", !bool);

				embedBuilder.setColor(Color.GREEN);
				embedBuilder.setDescription("Simple mode: `" + (!bool ? "Enabled" : "Disabled") + "`");

				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!version")) {
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("Currently running PandaEntus version (build date)", Version.BUILD_TIME, false);

				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}
		}
	}

}
