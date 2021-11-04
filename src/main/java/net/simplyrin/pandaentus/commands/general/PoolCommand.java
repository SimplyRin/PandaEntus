package net.simplyrin.pandaentus.commands.general;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

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
public class PoolCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!pool";
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
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		MessageChannel channel = event.getChannel();
		User user = event.getAuthor();

		if (args.length > 1) {
			if (instance.isBotOwner(user) && args.length > 3 && args[1].equalsIgnoreCase("set")) {
				String key = args[2];
				String game = "";
				for (int i = 3; i < args.length; i++) {
					game += args[i] + " ";
				}
				game = game.trim();

				instance.getPoolItems().setItem(key, game);

				embedBuilder.setColor(Color.GREEN);
				embedBuilder.setDescription("`" + key + "` を `" + game + "` として覚えました。");
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			int size = 0;
			for (int i = 1; i < args.length; i++) {
				if (i > 6) {
					break;
				}
				size = i;
				embedBuilder.addField(String.valueOf(i), instance.getPoolItems().getItem(args[i]), true);
			}

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("投票が開始されました。");
			Message message = channel.sendMessage(embedBuilder.build()).complete();

			for (int integer = 1; integer <= size; integer++) {
				String value = instance.getPoolItems().getReaction(integer);

				System.out.println("Add: " + value);
				message.addReaction(value).complete();
			}
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("使用方法: " + args[0] + " <1> <2> <3>... (max 6)");
		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
