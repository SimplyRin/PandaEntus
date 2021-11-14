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
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.TimeManager;

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
public class UptimeCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!uptime";
	}
	
	@Override
	public String getDescription() {
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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		Guild guild = event.getGuild();

		Category category = instance.getVoiceChannelCategory(event.getGuild());
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

		// General-2 チャンネルを取得
		GuildChannel guildChannel = category.getChannels().get(1);
		Date time = Date.from(guildChannel.getTimeCreated().toInstant());
		
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.addField("グループ合計通話時間", instance.getUptime(time), false);
		channel.sendMessage(embedBuilder.build()).complete();
	}



}
