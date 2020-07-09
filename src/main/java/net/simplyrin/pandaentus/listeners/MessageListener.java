package net.simplyrin.pandaentus.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.Main;

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

/**
 * そのうち削除する予定
 */
@Deprecated
public class MessageListener extends ListenerAdapter {

	private Main instance;
	private PrivateChatMessage privateChatMessage;

	public MessageListener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (this.privateChatMessage == null) {
			this.privateChatMessage = new PrivateChatMessage(this.instance);
		}
		this.privateChatMessage.onPrivateMessageReceived(event);
	}

	boolean notice = false;

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

		if (args.length > 0) {
			// 管理者向けコマンド
			String adminId = this.instance.getConfig().getString("Admin-ID");
			if (adminId.equals("") || adminId == null) {
				this.instance.getConfig().set("Admin-ID", "999");
				if (!this.notice) {
					System.out.println("Admin-ID が設定されていません。Config ファイルから Admin-ID を設定してください。");
					this.notice = true;
				}
				adminId = "999";
			}

			if (user.getId().equals(adminId)) {
				/* if (args[0].equalsIgnoreCase("!shutdown")) {
					Moved to ShutdownCommand.java
				} */

				/* if (args[0].equalsIgnoreCase("!init")) {
					Moved to InitCommand.java
				} */

				/* if (args[0].equalsIgnoreCase("!add-vc")) {
					Moved to AddVoiceChannelCommand.java
				} */

				/* if (args[0].equalsIgnoreCase("!setgame")) {
					Moved to SetGameCommand.java
				} */

				/* if (args[0].equalsIgnoreCase("!sendchat")) {
					Moved to SendChatCommand.java
				} */

				/* if (args[0].equalsIgnoreCase("!disk")) {
					Moved to DiskCommand.java
				} */
			}

			// General
			/* if (args[0].equalsIgnoreCase("!uptime")) {
				Moved to UptimeCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!pool")) {
				Moved to PoolCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!text-gen") || args[0].equalsIgnoreCase("!text-generate")) {
				Moved to TextGenCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!profile")) {
				Moved to ProfileCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!server")) {
				Moved to ServerCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!version")) {
				Moved to VersionCommand.java
			} */

			/* if (args[0].startsWith("=")) {
				Moved to CalcCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!ojichat")) {
				Moved to OjichatCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!vanish")) {
				Moved to VanishCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!akinator")) {
				Moved to AkinatorCommand.java
			} */

			/* if (args[0].startsWith("https://www.youtube.com/watch?v=") || args[0].startsWith("https://youtu.be/")) {
				Moved to YouTubeCommand.java, YouTubeWatchCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!emojis")) {
				Moved to EmojisCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!loop")) {
				Moved to LoopCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!play")) {
				Moved to YouTubePlayCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!skip")) {
				Moved to YouTubeSkipCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!dab")) {
				Moved to DabCommand.java
			} */

			/* if (args[0].equalsIgnoreCase("!kv")) {
				Moved to KvCommand.java
			} */
		}
	}

}
