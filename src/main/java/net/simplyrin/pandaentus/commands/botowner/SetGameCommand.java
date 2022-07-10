package net.simplyrin.pandaentus.commands.botowner;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class SetGameCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!setgame";
	}
	
	@Override
	public String getDescription() {
		return null;
	}
	
	@Override
	public CommandData getCommandData() {
		return null;
	}
	
	@Override
	public List<String> getAlias() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.BotOwner;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
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
				channel.sendMessageEmbeds(embedBuilder.build()).complete();
				return;
			}

			instance.getJda().getPresence().setActivity(Activity.playing(game));

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("Playing game has been set to '" + game + "'!");
			channel.sendMessageEmbeds(embedBuilder.build()).complete();
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("Usage: !setgame <game>");
		channel.sendMessageEmbeds(embedBuilder.build()).complete();
		return;
	}

}
