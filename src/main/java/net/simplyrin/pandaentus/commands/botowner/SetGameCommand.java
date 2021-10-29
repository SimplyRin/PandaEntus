package net.simplyrin.pandaentus.commands.botowner;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;

/**
 * Created by SimplyRin on 2020/07/09.
 *
 * Copyright (c) 2020 SimplyRin
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
public class SetGameCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!setgame";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public Permission getPermission() {
		return Permission.BotOwner;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();

		if (args.length > 1) {
			String game = "";
			for (int i = 1; i < args.length; i++) {
				game = game + args[i] + " ";
			}
			game = game.trim();

			if (game.equalsIgnoreCase("reset")) {
				instance.getJda().getPresence().setActivity(null);

				embedBuilder.setColor(Color.RED);
				embedBuilder.setDescription("Reset");
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			instance.getJda().getPresence().setActivity(Activity.playing(game));

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("Playing game has been set to '" + game + "'!");
			channel.sendMessage(embedBuilder.build()).complete();
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("Usage: !setgame <game>");
		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
