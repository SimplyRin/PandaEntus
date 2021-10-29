package net.simplyrin.pandaentus.commands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
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
public class PoolCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!pool";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public Permission getPermission() {
		return Permission.Everyone;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
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
