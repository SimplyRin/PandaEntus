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
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;
import net.simplyrin.pandaentus.utils.ThreadPool;

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
	public Permission getPermission() {
		return Permission.BotOwner;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
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
	
	public void printEmojis(Main instance, Guild guild, MessageChannel channel) {
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
