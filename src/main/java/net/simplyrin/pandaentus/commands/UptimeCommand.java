package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.utils.BaseCommand;
import net.simplyrin.pandaentus.utils.CommandType;
import net.simplyrin.pandaentus.utils.TimeManager;

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
public class UptimeCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!uptime";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		Guild guild = event.getGuild();

		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		MessageChannel channel = event.getChannel();
		User user = event.getAuthor();

		if (instance.getConfig().getBoolean("Disable")) {
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

			TimeManager timeManager = instance.getTimeManager().getUser(id);
			if ((!timeManager.isJoined()) || (!_if)) {
				embedBuilder.setColor(Color.RED);
				embedBuilder.setDescription("このユーザーは現在通話していません。");
				channel.sendMessage(embedBuilder.build()).complete();
				return;
			}

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setAuthor(instance.getNickname(event.getMember()), user.getAvatarUrl(), user.getAvatarUrl());
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
		embedBuilder.addField("グループ合計通話時間", instance.getUptime(time), false);
		channel.sendMessage(embedBuilder.build()).complete();
	}



}
