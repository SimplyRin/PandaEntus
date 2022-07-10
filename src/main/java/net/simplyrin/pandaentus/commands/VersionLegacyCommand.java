package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.util.List;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
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
public class VersionLegacyCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!version_legacy";
	}
	
	@Override
	public String getDescription() {
		return "PandaEntus のバージョンを確認";
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
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		embedBuilder.setColor(Color.GREEN);
		embedBuilder.addField("Version:", Version.BUILD_TIME, true);
		embedBuilder.addField("Server uptime", this.getUptime(instance), true);

		event.reply(embedBuilder.build());
		return;
	}
	
	public String getUptime(PandaEntus instance) {
		String uptime = "unknown";

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] {"uptime", "-p"});
			
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
		} catch (Exception e) {
			instance.postError(e);
		}
		
		return uptime;
	}

}
