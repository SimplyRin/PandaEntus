package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.utils.CallTimeManager;
import net.simplyrin.pandaentus.utils.CallTimeManager.CallTime;

/**
 * Created by SimplyRin on 2019/03/13.
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
public class Listener extends ListenerAdapter {

	private Main instance;
	private HashMap<String, CallTimeManager> map = new HashMap<>();

	public Listener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		Guild guild = event.getGuild();
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		Category parentCategory = event.getChannelJoined().getParent();

		Member member = event.getMember();

		System.out.println("[" + guild.getName() + "@" + guild.getId() + "] #" + event.getChannelJoined().getName() + " JOINED: " + this.getNickname(member) + "@" + member.getId());

		if (parentCategory != null) {
			if (!category.getName().equals(parentCategory.getName())) {
				return;
			}
		}

		this.instance.getTimeManager().getUser(event.getMember().getUser().getId()).joined();
		this.check(event.getMember(), event.getGuild());

		if (this.map.get(guild.getId()) == null) {
			this.map.put(guild.getId(), new CallTimeManager(this.instance, guild.getId()));
		}

		this.map.get(guild.getId()).join(member.getIdLong());
		System.out.println("Joined " + event.getChannelJoined().getName());
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		Member member = event.getMember();
		Guild guild = event.getGuild();

		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		List<VoiceChannel> voiceChannels = category.getVoiceChannels();

		boolean hasMember = false;
		for (VoiceChannel voiceChannel : voiceChannels) {
			for (Member vcMember : voiceChannel.getMembers()) {
				if (vcMember.getId().equalsIgnoreCase(member.getId())) {
					hasMember = true;
				}
			}
		}

		if (hasMember) {
			System.out.println("Join with Channel Moving " + this.getNickname(member));

			if (this.map.get(guild.getId()) == null) {
				this.map.put(guild.getId(), new CallTimeManager(this.instance, guild.getId()));
			}

			this.map.get(guild.getId()).join(member.getIdLong());
		} else {
			System.out.println("Quit with Channel Moving " + this.getNickname(member));

			this.quitMember(guild, category, null, member, event.getChannelLeft(), false);
		}

		this.check(event.getMember(), event.getGuild());
	}

	public void check(Member member, Guild guild) {
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		List<VoiceChannel> voiceChannels = category.getVoiceChannels();

		boolean[] max = new boolean[voiceChannels.size()];
		int current = 0;

		int membersSize = 0;
		for (VoiceChannel voiceChannel : voiceChannels) {
			for (Member vcMember : voiceChannel.getMembers()) {
				if ((!vcMember.getUser().isBot()) && (!vcMember.getUser().isFake())) {
					membersSize++;
				}
			}
			if (membersSize >= 1) {
				max[current] = true;
			}

			current++;

			membersSize = 0;
		}

		int count = 0;
		for (boolean m : max) {
			if (m) {
				count++;
			}
		}

		if (voiceChannels.size() == count) {
			int c = (count + 1);
			VoiceChannel voiceChannel = category.createVoiceChannel("General-" + c).complete();
			voiceChannel.getManager().setUserLimit(99).complete();
			if (c == 2) {
				this.instance.getGuildCallManager(voiceChannel.getId()).joined(member.getId());
			}
		}
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		Guild guild = event.getGuild();
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		Category parentCategory = event.getChannelLeft().getParent();

		Member member = event.getMember();

		this.quitMember(guild, category, parentCategory, member, event.getChannelLeft(), true);
	}

	public void quitMember(Guild guild, Category category, Category parentCategory, Member member, VoiceChannel channelLeft, boolean leaveEvent) {
		System.out.println("[" + guild.getName() + "@" + guild.getId() + "] #" + channelLeft.getName() + " LEAVE: " + this.getNickname(member) + "@" + member.getId());

		if (leaveEvent) {
			if (parentCategory == null) {
				return;
			}
			if (!category.getName().equals(parentCategory.getName())) {
				return;
			}
		}

		this.instance.getTimeManager().getUser(member.getUser().getId()).quit();

		if (this.map.get(guild.getId()) != null) {
			this.map.get(guild.getId()).quit(member.getIdLong());
		}

		List<VoiceChannel> voiceChannels = category.getVoiceChannels();

		if (voiceChannels.size() == 1) {
			return;
		}

		boolean[] free = new boolean[voiceChannels.size()];
		int current = 0;

		int membersSize = 0;
		for (VoiceChannel voiceChannel : voiceChannels) {
			for (Member vcMember : voiceChannel.getMembers()) {
				if ((!vcMember.getUser().isBot()) && (!vcMember.getUser().isFake())) {
					membersSize++;
				}
			}
			if (membersSize == 0) {
				free[current] = true;
			}

			current++;

			membersSize = 0;
		}

		int count = 0;
		for (boolean m : free) {
			if (m) {
				count++;
			}
		}

		if (voiceChannels.size() == count) {
			try {
				GuildChannel guildChannel = (GuildChannel) category.getChannels().get(1);
				Date time = Date.from(guildChannel.getTimeCreated().toInstant());

				Category textChannels = (Category) guild.getCategoriesByName("Text Channels", true).get(0);
				TextChannel textChannel = (TextChannel) textChannels.getChannels().get(0);

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);
				if (this.instance.getConfig().getBoolean("Message-Type.Enable-Simple-Mode")) {
					embedBuilder.setAuthor("通話時間: " + this.instance.getUptime(time));

					List<CallTime> list = new ArrayList<>();

					Collection<CallTime> callTimes = this.map.get(guild.getId()).getMap().values();
					for (CallTime callTime : callTimes) {
						String path = "User." + callTime.getUser() + "." + guild.getId() + ".Vanish";
						if (this.instance.getConfig().getBoolean(path)) {
							continue;
						}

						try {
							embedBuilder.addField(this.getNickname(guild.getMemberById(callTime.getUser())), callTime.getTime().toString(), true);
							embedBuilder.setDescription("ユーザーごとの通話時間:");
						} catch (Exception e) {
						}
						list.add(callTime);

						callTime.resetTime();
					}
				} else {
					embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
					embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true);
					embedBuilder.addField("終了時刻", this.instance.getNowTime(), true);
				}

				this.map.get(guild.getId()).getMap().clear();

				if (this.instance.getConfig().getBoolean("Disable")) {
					return;
				}

				textChannel.sendMessage(embedBuilder.build()).complete();
			} catch (Exception e) {
				GuildChannel guildChannel = (GuildChannel) category.getChannels().get(1);
				Date time = Date.from(guildChannel.getTimeCreated().toInstant());

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true);
				embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
				embedBuilder.addField("終了時刻", this.instance.getNowTime(), true);

				this.instance.postError(e);
			}

			for (VoiceChannel voiceChannel : voiceChannels) {
				try {
					voiceChannel.delete().complete();
				} catch (Exception e) {
					this.instance.postError(e);
				}
			}

			category.createVoiceChannel("General-1").complete().getManager().setUserLimit(99).complete();
		}
	}

	public String getNickname(Member member) {
		if (member.getNickname() != null) {
			return member.getNickname();
		}
		return member.getUser().getName();
	}

}
