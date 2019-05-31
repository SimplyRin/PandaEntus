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
				embedBuilder.setDescription("Currently running PandaEntus version (build date): " + Version.BUILD_TIME + " (Asia/Tokyo)");

				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}
		}
	}

}
