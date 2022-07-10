package net.simplyrin.pandaentus.commands;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

/**
 * Created by SimplyRin on 2020/11/25.
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
public class InstaReelCommand extends BaseCommand {

	public static void main(String[] args) {
		new InstaReelCommand().execute(null, null, new String[] { "https://www.instagram.com/reel/CbxCszjp4WL/" }); // ←かわいい
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
	public String getCommand() {
		return "https://www.instagram.com/reel/";
	}

	@Override
	public CommandType getType() {
		return CommandType.StartsWith;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		String url = args[0];
		if (url.contains(Pattern.quote("?"))) {
			url = url.split(Pattern.quote("?"))[0];
		}
		
		var id = UUID.randomUUID().toString().split("-")[0];

		System.out.println("[InstaReelCommand] [" + id + "] Checking: " + url);
		
		String youtubeDlPathTemp = "youtube-dl";
		File youtubeDl = new File("youtube-dl");
		if (youtubeDl.exists()) {
			youtubeDlPathTemp = youtubeDl.getAbsolutePath();
		}
		
		ProcessManager.runCommand(new String[]{ youtubeDlPathTemp, "--get-url", url }, new Callback() {
			@Override
			public void line(String response) {
				System.out.println("[InstaReel] [" + id + "] " + response);
				
				if (response.toLowerCase().startsWith("https://")) {
					event.getChannel().sendMessage(response).complete();
				}
			}
		}, true);
	}

}
