package net.simplyrin.pandaentus.commands;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.simplyrin.pandaentus.utils.ThreadPool;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

/**
 * Created by SimplyRin on 2020/11/23.
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
public class TikTokCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "https://vt.tiktok.com/";
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
		return CommandType.StartsWith;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		File file = new File(new File("tiktok"), "api.php");
		ProcessManager.runCommand(new String[] { "/usr/bin/php", file.getAbsolutePath(), event.getMessage().getContentRaw() }, new Callback() {
			@Override
			public void line(String response) {
				System.out.println("TikTokCommand.java: " + response);

				MessageChannel channel = event.getChannel();

				boolean mention = false;
				if (event.getGuild().getTextChannelsByName("tiktok", true) != null) {
					mention = true;
					channel = (MessageChannel) event.getGuild().getTextChannelsByName("tiktok", true).get(0);
				}

				System.out.println("TikTokCommand.java: TikTok URL sending to #" + channel.getName());

				JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
				Message message = channel.sendMessage((mention ? event.getAuthor().getAsMention() + " " : "") + jsonObject.get("url").getAsString()).complete();

				String path = jsonObject.get("path").getAsString();
				File file = new File(path);
				file.deleteOnExit();

				ThreadPool.run(() -> {
					try {
						TimeUnit.DAYS.sleep(1);
					} catch (Exception e) {
					}

					System.out.println("Delete: " + path);
					file.delete();
					message.delete().complete();
				});
			}
		}, true);
	}

}
