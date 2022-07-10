package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class PoolCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!pool";
	}
	
	@Override
	public String getDescription() {
		return "投票用プールメッセージを作成";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("pool", this.getDescription())
				.addOption(OptionType.STRING, "1", "1個目のアイテム", true)
				.addOption(OptionType.STRING, "2", "2個目のアイテム", true)
				.addOption(OptionType.STRING, "3", "3個目のアイテム")
				.addOption(OptionType.STRING, "4", "4個目のアイテム")
				.addOption(OptionType.STRING, "5", "5個目のアイテム")
				.addOption(OptionType.STRING, "6", "6個目のアイテム");
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
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[7];
			args[0] = this.getCommand();
			args[1] = s.getOption("1").getAsString();
			args[2] = s.getOption("2").getAsString();
			args[3] = s.getOption("3") != null ? s.getOption("3").getAsString() : null;
			args[4] = s.getOption("4") != null ? s.getOption("4").getAsString() : null;
			args[5] = s.getOption("5") != null ? s.getOption("5").getAsString() : null;
			args[6] = s.getOption("6") != null ? s.getOption("6").getAsString() : null;
		}

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
				
				event.reply(embedBuilder.build());
				return;
			}

			int size = 0;
			for (int i = 1; i < args.length; i++) {
				if (i > 6) {
					break;
				}
				size = i;
				if (args[i] == null) {
					continue;
				}

				embedBuilder.addField(String.valueOf(i), instance.getPoolItems().getItem(args[i]), true);
			}

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("投票が開始されました。");
			Message message = event.reply(embedBuilder.build());

			for (int integer = 1; integer <= size; integer++) {
				String value = instance.getPoolItems().getReaction(integer);

				System.out.println("Add: " + value);

				message.addReaction(Emoji.fromFormatted(value)).complete();
			}
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("使用方法: " + args[0] + " <1> <2> <3>... (最大: 6)");
		event.reply(embedBuilder.build());
		return;
	}

}
