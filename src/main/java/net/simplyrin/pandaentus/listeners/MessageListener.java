package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.utils.TimeManager;

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
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return;
		}

		Guild guild = event.getGuild();
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		MessageChannel channel = event.getChannel();

		String[] args = event.getMessage().getContentRaw().split(" ");

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("!uptime")) {
				if (args.length > 1) {
					String id = args[1].replace("<@", "").replace(">", "");

					if (id.length() != 18) {
						id = event.getAuthor().getId();
					}

					TimeManager timeManager = this.instance.getTimeManager().getUser(id);
					if (!timeManager.isJoined()) {
						channel.sendMessage("このユーザーは現在通話していません。").complete();
						return;
					}

					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setColor(Color.GREEN);
					embedBuilder.addField("参加時間", timeManager.getJoinedTime(), true);
					embedBuilder.addField("通話時間", timeManager.getCurrentTime(), true);

					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				List<GuildChannel> channels = category.getChannels();
				if (channels.size() == 1) {
					EmbedBuilder embedBuilder = new EmbedBuilder();
					embedBuilder.setDescription("現在通話していません。");
					embedBuilder.setColor(Color.GREEN);
					channel.sendMessage(embedBuilder.build()).complete();
					return;
				}

				GuildChannel guildChannel = category.getChannels().get(1);
				Date time = Date.from(guildChannel.getTimeCreated().toInstant());

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("現在の通話時間", this.instance.getUptime(time), true);
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}
		}
	}

}
