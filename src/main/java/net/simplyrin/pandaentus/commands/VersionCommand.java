package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.Version;

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
public class VersionCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!version";
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
		MessageChannel channel = event.getChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();

		embedBuilder.setColor(Color.GREEN);
		embedBuilder.addField("Version:", Version.BUILD_TIME, true);

		String uptime = "unknown";

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] {"uptime", "-p"});
		} catch (Exception e) {
			instance.postError(e);
			return;
		}
		Scanner scanner = new Scanner(process.getInputStream());
		if (scanner.hasNext()) {
			uptime = scanner.nextLine();

			uptime = uptime.replace("up ", "");
			uptime = uptime.replace(",", "");
			uptime = uptime.replace(" years", "年");
			uptime = uptime.replace(" year", "年");

			uptime = uptime.replace(" weeks", "週間");
			uptime = uptime.replace(" week", "週間");
			uptime = uptime.replace(" days", "日");
			uptime = uptime.replace(" day", "日");
			uptime = uptime.replace(" hours", "時間");
			uptime = uptime.replace(" hour", "時間");
			uptime = uptime.replace(" minutes", "分");
			uptime = uptime.replace(" minute", "分");
		}
		scanner.close();
		embedBuilder.addField("Server uptime", uptime, true);

		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
