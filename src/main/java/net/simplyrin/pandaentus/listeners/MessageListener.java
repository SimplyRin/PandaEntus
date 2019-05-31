package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.httpclient.HttpClient;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.utils.TimeManager;
import net.simplyrin.pandaentus.utils.Version;

/**
 * Created by SimplyRin on 2019/04/04.
 *
 * Copyright (c) 2019 SimplyRin
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
public class MessageListener extends ListenerAdapter {

	private Main instance;

	public MessageListener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		new PrivateChatMessage(this.instance).onPrivateMessageReceived(event);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return;
		}

		User user = event.getAuthor();
		Guild guild = event.getGuild();
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		MessageChannel channel = event.getChannel();

		String[] args = event.getMessage().getContentRaw().split(" ");

		EmbedBuilder embedBuilder = new EmbedBuilder();

		if (args.length > 0) {
			// Admin
			if (user.getId().equals("224428706209202177")) {
				if (args[0].equalsIgnoreCase("!add-vc")) {
					if (args.length > 1) {
						int i;
						try {
							i = Integer.valueOf(args[1]).intValue();
						} catch (Exception e) {
							embedBuilder.setColor(Color.RED);
							embedBuilder.setDescription("Invalid usage!");
							channel.sendMessage(embedBuilder.build()).complete();
							return;
						}

						List<VoiceChannel> voiceChannels = category.getVoiceChannels();
						int size = voiceChannels.size() + 1;

						int count = 0;
						while (true) {
							if (i == count) {
								break;
							}

							try {
								VoiceChannel voiceChannel = category.createVoiceChannel("General-" + size).complete();
								voiceChannel.getManager().setUserLimit(99).complete();
							} catch (Exception e) {
							}

							if (size == 50) {
								break;
							}

							size++;
							count++;
						}

						embedBuilder.setColor(Color.GREEN);
						embedBuilder.setDescription("Created!");
						channel.sendMessage(embedBuilder.build()).complete();
					}
				}
			}

			// General
			if (args[0].equalsIgnoreCase("!uptime")) {
				if (this.instance.getConfig().getBoolean("Disable")) {
					embedBuilder.setColor(Color.RED);
					embedBuilder.setDescription("この機能は現在一時的に無効にされています。");
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				if (args.length > 1) {
					String id = args[1].replace("<@", "").replace(">", "");

					if (id.length() != 18) {
						id = user.getId();
					}

					boolean _if = false;
					for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
						for (Member member : voiceChannel.getMembers()) {
							if (member.getId().equals(id)) {
								_if = true;
							}
						}
					}
					if (!_if) {
						embedBuilder.setColor(Color.RED);
						embedBuilder.setDescription("このユーザーは現在通話していません。");
						channel.sendMessage(embedBuilder.build()).complete();
						return;
					}

					TimeManager timeManager = this.instance.getTimeManager().getUser(id);
					if ((!timeManager.isJoined()) || (!_if)) {
						embedBuilder.setColor(Color.RED);
						embedBuilder.setDescription("このユーザーは現在通話していません。");
						channel.sendMessage(embedBuilder.build()).complete();
						return;
					}

					embedBuilder.setColor(Color.GREEN);
					embedBuilder.setAuthor(this.getNickname(event.getMember()), user.getAvatarUrl(), user.getAvatarUrl());
					embedBuilder.addField("参加時間", timeManager.getJoinedTime(), true);
					embedBuilder.addField("通話時間", timeManager.getCurrentTime(), true);

					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				List<GuildChannel> channels = category.getChannels();
				if (channels.size() == 1) {
					embedBuilder.setDescription("現在通話していません。");
					embedBuilder.setColor(Color.GREEN);
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				GuildChannel guildChannel = category.getChannels().get(1);
				Date time = Date.from(guildChannel.getTimeCreated().toInstant());

				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("グループ合計通話時間", this.instance.getUptime(time), false);
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!text-gen") || args[0].equalsIgnoreCase("!text-generate")) {
				if (args.length > 1) {
					String text = args[1];
					channel.sendTyping().complete();

					HttpClient httpClient = new HttpClient("https://ja.cooltext.com/PostChange");

					// httpClient.addHeader("cookie", "_ga=GA1.2.1279499266.1558787537; _gid=GA1.2.1065377376.1558787537; ASP.NET_SessionId=" + UUID.randomUUID().toString().split("-")[0]);
					httpClient.addHeader("origin", "https://ja.cooltext.com");
					httpClient.addHeader("accept-encoding", "gzip, deflate, br");
					httpClient.addHeader("accept-language", "ja-JP,ja;q=0.9,en;q=0.8,zh-CN;q=0.7,zh;q=0.6");
					httpClient.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
					httpClient.addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
					httpClient.addHeader("accept", "*/*");
					httpClient.addHeader("referer", "https://ja.cooltext.com/Logo-Design-Particle");
					httpClient.addHeader("authority", "ja.cooltext.com");
					httpClient.addHeader("x-requested-with", "XMLHttpRequest");

					// httpClient.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
					httpClient.setData("LogoID=39&Text=" + text + "&FontSize=70&Color1_color=23320000&Integer5=3&Integer7=0&Integer8=0&Integer14_color=#23000000^&Integer6=95&Integer9=0&Integer13=on&Integer12=on&FileFormat=6&BackgroundColor_color=#23FFFFFF");
					JsonObject result = httpClient.getAsJsonObject();
					System.out.println("Result: " + result);

					embedBuilder.setColor(Color.GREEN);
					embedBuilder.setDescription("Image generated!");
					embedBuilder.setImage(result.get("renderLocation").getAsString());
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				embedBuilder.setColor(Color.RED);
				embedBuilder.setDescription("使用方法: " + args[0] + " <テキスト>");
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!version")) {
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("Currently running PandaEntus version (build date)", Version.BUILD_TIME + " (Asia/Toyo)", true);

				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}
		}
	}

	public String getNickname(Member member) {
		if (member.getNickname() != null) {
			return member.getNickname();
		}
		return member.getUser().getName();
	}

}
