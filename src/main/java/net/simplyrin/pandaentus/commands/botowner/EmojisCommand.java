package net.simplyrin.pandaentus.commands.botowner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FilenameUtils;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Icon.IconType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.ThreadPool;

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
public class EmojisCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!emojis";
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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		Guild guild = event.getGuild();
		MessageChannel channel = event.getChannel();
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("listguilds")) {
				String message = "```\n";
				for (Guild g : instance.getJda().getGuilds()) {
					message += g.getId() + " (" + g.getName() + ")\n";
				}
				channel.sendMessage(message + "```").complete();
				return;
			}
			
			if (args[1].equalsIgnoreCase("list")) {
				if (args.length > 2) {
					Guild g = instance.getJda().getGuildById(args[2]);
					if (g != null) {
						this.printEmojis(instance, g, channel);
						return;
					}
					
					channel.sendMessage("Guild が見つかりませんでした。").complete();
					return;
				}
				
				channel.sendMessage(this.getCommand() + " " + args[1] + " <guildId>").complete();
				return;
			}
			if (args[1].equalsIgnoreCase("upload")) {
				if (args.length > 4) {
					String guildId = args[2];
					Guild g = instance.getJda().getGuildById(guildId);
					if (g == null) {
						channel.sendMessage("Guild が見つかりませんでした。").complete();
						return;
					}
					
					ThreadPool.run(() -> {
						InputStream inputStream;
						try {
							HttpsURLConnection connection = (HttpsURLConnection) new URL(args[4]).openConnection();
							connection.addRequestProperty("user-agent", instance.getBotUserAgent());
							connection.connect();
							
							inputStream = connection.getInputStream();
						} catch (Exception e) {
							e.printStackTrace();
							channel.sendMessage("エラーが発生しました").complete();
							return;
						}
						
						try {
							String extension = FilenameUtils.getExtension(args[4]);
							
							Icon icon = Icon.from(inputStream, IconType.fromExtension(extension));
							g.createEmote(args[3], icon).complete();
							
							channel.sendMessage("絵文字をアップロードしました。").complete();
						} catch (IOException e) {
							e.printStackTrace();
							channel.sendMessage("エラーが発生しました").complete();
						}
					});
					return;
				}
				
				channel.sendMessage(this.getCommand() + " " + args[1] + " <guildId> <emojiName> <emojiUrl>").complete();
				return;
			}
		}
		
		this.printEmojis(instance, guild, channel);
	}
	
	public void printEmojis(PandaEntus instance, Guild guild, MessageChannel channel) {
		String message = "";
		for (Emote emote : guild.getEmotes()) {
			message += emote.getName() + " -> " + emote.getId() + ", " + emote.getImageUrl() + "\n";
		}
		
		if (message.length() >= 2000) {
			String fm = message;
			ThreadPool.run(() -> {
				File temp = instance.stringToTempFile(fm);
				
				channel.sendFile(temp).complete();
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				temp.delete();
			});
		} else {
			channel.sendMessage(message).complete();
		}
	}

}
