package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
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
import net.simplyrin.pandaentus.utils.TimeUtils.CallTime;

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
	// private HashMap<String, CallTimeManager> map = new HashMap<>();

	public Listener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		if (event.getMember().getUser().isBot() || event.getMember().getUser().isFake()) {
			return;
		}

		Guild guild = event.getGuild();
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		List<VoiceChannel> voiceChannels = category.getVoiceChannels();
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

		boolean hasMember = false;
		for (VoiceChannel voiceChannel : voiceChannels) {
			for (Member vcMember : voiceChannel.getMembers()) {
				if (vcMember.getId().equalsIgnoreCase(member.getId())) {
					hasMember = true;
				}
			}
		}

		if (hasMember) {
			CallTime timeUtils = this.instance.getTimeUtils().get(guild.getId(), member.getId());
			timeUtils.join();
		}
		System.out.println("Joined " + event.getChannelJoined().getName());
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		if (event.getMember().getUser().isBot() || event.getMember().getUser().isFake()) {
			return;
		}
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

		CallTime timeUtils = this.instance.getTimeUtils().get(guild.getId(), member.getId());
		if (hasMember) {
			System.out.println("Join @ " + this.getNickname(member));
			timeUtils.join();
		} else {
			timeUtils.quit();
			System.out.println("Quit @ " + this.getNickname(member));

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
				if (vcMember.getUser().isBot() || vcMember.getUser().isFake()) {
					continue;
				}
				membersSize++;
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
			category.createVoiceChannel("General-" + c).setUserlimit(99).complete();
		}
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		if (event.getMember().getUser().isBot() || event.getMember().getUser().isFake()) {
			return;
		}
		Guild guild = event.getGuild();
		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		Category parentCategory = event.getChannelLeft().getParent();

		Member member = event.getMember();

		CallTime timeUtils = this.instance.getTimeUtils().get(guild.getId(), member.getUser().getId());
		timeUtils.quit();

		this.quitMember(guild, category, parentCategory, event.getMember(), event.getChannelLeft(), true);
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

					for (CallTime callTime : this.instance.getTimeUtils().getList(guild.getId())) {
						String path = "User." + callTime.getName() + "." + guild.getId() + ".Vanish";
						if (this.instance.getConfig().getBoolean(path)) {
							continue;
						}

						try {
							String tt = callTime.getTime();
							if (!tt.equals("0秒")) {
								embedBuilder.setDescription("ユーザーごとの通話時間:");
								embedBuilder.addField(this.getNickname(guild.getMemberById(callTime.getName())), callTime.getTime(), true);
							}

							System.out.println(callTime.getName() + " -> " + callTime.getTime());
						} catch (Exception e) {
						}
						list.add(callTime);

						this.instance.getTimeUtils().resetGuild(guild.getId());
					}
				} else {
					embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
					embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true);
					embedBuilder.addField("終了時刻", this.instance.getNowTime(), true);
				}

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

			category.createVoiceChannel("General-1").setUserlimit(99).complete();
		}
	}

	public String getNickname(Member member) {
		if (member.getNickname() != null) {
			return member.getNickname();
		}
		return member.getUser().getName();
	}

}
