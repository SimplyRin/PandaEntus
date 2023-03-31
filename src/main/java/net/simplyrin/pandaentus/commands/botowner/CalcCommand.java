package net.simplyrin.pandaentus.commands.botowner;

import java.awt.Color;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
public class CalcCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "=";
	}

	@Override
	public CommandType getType() {
		return CommandType.StartsWith;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.BotOwner;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		MessageChannel channel = event.getChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();

		String input = args[0].replace("=", "");
		if (input.length() == 0) {
			embedBuilder.setColor(Color.RED);
			embedBuilder.setDescription("使用方法: =<計算式>\n=1+1");
			channel.sendMessageEmbeds(embedBuilder.build()).complete();
			return;
		}

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] {"calc", input});
		} catch (Exception e) {
			instance.postError(e);
			return;
		}
		Scanner scanner = new Scanner(process.getInputStream());
		if (scanner.hasNext()) {
			channel.sendMessage("結果: **" + scanner.nextLine().trim() + "**").complete();
		}
		scanner.close();
	}

}
