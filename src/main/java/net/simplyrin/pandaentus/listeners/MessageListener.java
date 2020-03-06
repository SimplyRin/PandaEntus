package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import com.github.ucchyocean.lc.japanize.IMEConverter;
import com.github.ucchyocean.lc.japanize.YukiKanaConverter;
import com.google.gson.JsonObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.httpclient.HttpClient;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.utils.TimeManager;
import net.simplyrin.pandaentus.utils.Version;

/**
 * Created by SimplyRin on 2019/04/04.
 *
 * Copyright (C) 2019 SimplyRin
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
public class MessageListener extends ListenerAdapter {

	private Main instance;

	public MessageListener(Main instance) {
		this.instance = instance;
	}

	private PrivateChatMessage privateChatMessage;

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (this.privateChatMessage == null) {
			this.privateChatMessage = new PrivateChatMessage(this.instance);
		}
		this.privateChatMessage.onPrivateMessageReceived(event);
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

		String raw = event.getMessage().getContentRaw();
		String[] args = raw.split(" ");

		EmbedBuilder embedBuilder = new EmbedBuilder();

		if (channel.getName().equalsIgnoreCase("translate")) {
			if (user.isBot()) {
				return;
			}

			channel.sendMessage(IMEConverter.convByGoogleIME(YukiKanaConverter.conv(raw))).complete();
			return;
		}

		/* if (channel.getName().equalsIgnoreCase("english-to-japanese")) {
			if (user.isBot()) {
				return;
			}

			String result = HttpClient.fetch("https://script.google.com/macros/s/AKfycbzhliArfSms9uqI76PZS2dblAJV588-W19oBGO9g_-gCiznO3yI/exec?text=" + raw + "&target=ja");
			System.out.println(result);
			if (result.length() >= 1500) {
				return;
			}

			channel.sendMessage(result).complete();
			return;
		} */

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
						return;
					}
				}

				if (args[0].equalsIgnoreCase("!setgame")) {
					if (args.length > 1) {
						String game = "";
						for (int i = 1; i < args.length; i++) {
							game = game + args[i] + " ";
						}
						game = game.trim();

						if (game.equalsIgnoreCase("reset")) {
							this.instance.getJda().getPresence().setActivity(null);

							embedBuilder.setColor(Color.RED);
							embedBuilder.setDescription("Reset");
							channel.sendMessage(embedBuilder.build()).complete();
							return;
						}

						this.instance.getJda().getPresence().setActivity(Activity.playing(game));

						embedBuilder.setColor(Color.GREEN);
						embedBuilder.setDescription("Playing game has been set to '" + game + "'!");
						channel.sendMessage(embedBuilder.build()).complete();
						return;
					}

					embedBuilder.setColor(Color.RED);
					embedBuilder.setDescription("Usage: !setgame <game>");
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				if (args[0].equalsIgnoreCase("!sendchat")) {
					if (args.length > 3) {
						Guild g = this.instance.getJda().getGuildById(args[1]);
						MessageChannel mc = g.getTextChannelById(args[2]);

						String name = "";
						for (int i = 3; i < args.length; i++) {
							name = name + args[i] + " ";
						}
						mc.sendMessage(name.trim()).complete();
						return;
					}

					embedBuilder.setColor(Color.RED);
					embedBuilder.setDescription("Usage: !sendchat <guild> <channel> <message>");
					channel.sendMessage(embedBuilder.build()).complete();
					return;
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

			if (args[0].equalsIgnoreCase("!pool")) {
				if (args.length > 1) {
					if (user.getId().equals("224428706209202177") && args.length > 3 && args[1].equalsIgnoreCase("set")) {
						String key = args[2];
						String game = "";
						for (int i = 3; i < args.length; i++) {
							game += args[i] + " ";
						}
						game = game.trim();

						this.instance.getPoolItems().setItem(key, game);

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
						embedBuilder.addField(String.valueOf(i), this.instance.getPoolItems().getItem(args[i]), true);
					}

					embedBuilder.setColor(Color.GREEN);
					embedBuilder.setDescription("投票が開始されました。");
					Message message = channel.sendMessage(embedBuilder.build()).complete();

					for (int integer = 1; integer <= size; integer++) {
						String value = this.instance.getPoolItems().getReaction(integer);

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

			if (args[0].equalsIgnoreCase("!profile")) {
				embedBuilder.setColor(Color.ORANGE);

				Member member = event.getMember();
				if (args.length > 1) {
					try {
						member = guild.getMemberById(args[1]);
					} catch (Exception e) {
					}
				}

				Date date = new Date(member.getUser().getTimeCreated().toInstant().toEpochMilli());
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
				embedBuilder.addField("アカウント名", this.getNickname(member), false);
				embedBuilder.addField("アカウント作成日", simpleDateFormat.format(date), false);
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!server")) {
				embedBuilder.setColor(Color.ORANGE);

				Date date = new Date(guild.getTimeCreated().toInstant().toEpochMilli());
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
				embedBuilder.addField("サーバー作成日", simpleDateFormat.format(date), true);
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			if (args[0].equalsIgnoreCase("!version")) {
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("Version:", Version.BUILD_TIME, true);

				String uptime = "unknown";

				Runtime runtime = Runtime.getRuntime();
				Process process = null;
				try {
					process = runtime.exec(new String[] {"uptime", "-p"});
				} catch (Exception e) {
					this.instance.postError(e);
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

			if (args[0].startsWith("=")) {
				String input = args[0].replace("=", "");
				if (input.length() == 0) {
					embedBuilder.setColor(Color.RED);
					embedBuilder.setDescription("使用方法: =<計算式>\n=1+1");
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				Runtime runtime = Runtime.getRuntime();
				Process process = null;
				try {
					process = runtime.exec(new String[] {"calc", input});
				} catch (Exception e) {
					this.instance.postError(e);
					return;
				}
				Scanner scanner = new Scanner(process.getInputStream());
				if (scanner.hasNext()) {
					channel.sendMessage("結果: **" + scanner.nextLine().trim() + "**").complete();
				}
				scanner.close();
			}

			if (args[0].equalsIgnoreCase("!jp")) {
				if (args.length > 1) {
					String text = "";
					for (int i = 1; i < args.length; i++) {
						text = text + args[i] + " ";
					}

					embedBuilder.setColor(Color.GREEN);
					embedBuilder.setDescription(IMEConverter.convByGoogleIME(YukiKanaConverter.conv(text)));
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				embedBuilder.setColor(Color.RED);
				embedBuilder.setDescription("Usage: !jp <text>");
				channel.sendMessage(embedBuilder.build()).complete();
			}

			if (args[0].equalsIgnoreCase("!ojichat")) {
				String name = "";
				for (int i = 1; i < args.length; i++) {
					name = name + args[i] + " ";
				}

				boolean normalText = false;

				name = name.trim();

				// テキトー！！
				if (name.contains("-normal") || name.contains("-n")) {
					normalText = true;
					name = name.replace("-normal", "");
					name = name.replace("-n", "");
				}

				name = name.trim();

				HttpClient httpClient = new HttpClient("https://ojichat.appspot.com/post");
				httpClient.setData("name=" + name + "&emoji_level=4&punctuation_level=0");

				httpClient.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");

				httpClient.addHeader("Accept", "application/json, text/plain, */*");
				httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
				httpClient.addHeader("Origin", "https://oji.netlify.com");
				httpClient.addHeader("Referer", "https://oji.netlify.com/");

				String result;
				try {
					result = httpClient.getAsJsonObject().get("message").getAsString();
				} catch (Exception e) {
					return;
				}

				embedBuilder.setColor(Color.GREEN);
				embedBuilder.setDescription(result);

				if (normalText) {
					channel.sendMessage(result).complete();
				} else {
					channel.sendMessage(embedBuilder.build()).complete();
				}
				return;
			}

			if (args[0].equalsIgnoreCase("!vanish")) {
				Configuration config = this.instance.getConfig();

				String path = "User." + user.getId() + "." + guild.getId() + ".Vanish";

				boolean bool = config.getBoolean(path);
				bool = !bool;

				this.instance.getConfig().set(path, bool);

				embedBuilder.setColor(Color.GRAY);
				embedBuilder.setDescription("You are now " + (bool ? "vanished" : "unvanished") + ".");

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
