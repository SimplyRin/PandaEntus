package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.commands.serveradmin.VoiceOnlyChatCommand;
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

	private PandaEntus instance;
	// private HashMap<String, CallTimeManager> map = new HashMap<>();

	public Listener(PandaEntus instance) {
		this.instance = instance;
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		var member = event.getMember();
		
		if (member.getUser().isBot()) {
			return;
		}
		
		// 通話参加者専用チャンネル管理
		var voc = (VoiceOnlyChatCommand) this.instance.getCommandRegister().getRegisteredCommand(VoiceOnlyChatCommand.class);
		voc.join(this.instance, member);

		Guild guild = event.getGuild();
		Category category = this.instance.getVoiceChannelCategory(guild);
		
		if (category == null) {
			return;
		}
		
		List<VoiceChannel> voiceChannels = category.getVoiceChannels();
		Category parentCategory = guild.getVoiceChannelById(event.getChannelJoined().getId()).getParentCategory();

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
		
		this.instance.getVcNameManager().getJoinedChannel().put(member.getIdLong(), event.getChannelJoined().getIdLong());
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
		if (event.getMember().getUser().isBot()) {
			return;
		}
		Member member = event.getMember();
		Guild guild = event.getGuild();
		
		this.instance.getVcNameManager().getJoinedChannel().put(member.getIdLong(), event.getChannelJoined().getIdLong());

		Category category = this.instance.getVoiceChannelCategory(guild);
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

			this.quitMember(guild, category, null, member, guild.getVoiceChannelById(event.getChannelLeft().getId()), false);
		}

		this.check(event.getMember(), event.getGuild());
	}

	public void check(Member member, Guild guild) {
		Category category = this.instance.getVoiceChannelCategory(guild);
		List<VoiceChannel> voiceChannels = category.getVoiceChannels();

		boolean[] max = new boolean[voiceChannels.size()];
		int current = 0;

		int membersSize = 0;
		for (VoiceChannel voiceChannel : voiceChannels) {
			for (Member vcMember : voiceChannel.getMembers()) {
				if (vcMember.getUser().isBot()) {
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
		
		String path = "Server." + guild.getId() + ".DisableChannelManagement";
		
		if (!this.instance.getConfig().getBoolean(path)) {
			if (voiceChannels.size() == count) {
				int c = (count + 1);
				category.createVoiceChannel(this.instance.getVoiceChannelName(category) + "-" + c).setUserlimit(99).setBitrate(guild.getMaxBitrate()).complete();
			}
		}
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		var member = event.getMember();
		
		if (event.getMember().getUser().isBot()) {
			return;
		}
		
		// 通話参加者専用チャンネル管理
		var voc = (VoiceOnlyChatCommand) this.instance.getCommandRegister().getRegisteredCommand(VoiceOnlyChatCommand.class);
		voc.quit(this.instance, member);
		
		// ちゃんねるめい
		this.instance.getVcNameManager().updateVoiceChannelName(event.getMember(), null);
		

		Guild guild = event.getGuild();
		Category category = this.instance.getVoiceChannelCategory(guild);
		if (category == null) {
			return;
		}
		Category parentCategory = guild.getVoiceChannelById(event.getChannelLeft().getId()).getParentCategory();

		CallTime timeUtils = this.instance.getTimeUtils().get(guild.getId(), member.getUser().getId());
		timeUtils.quit();
		
		this.quitMember(guild, category, parentCategory, event.getMember(), guild.getVoiceChannelById(event.getChannelLeft().getId()), true);
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
				if (!vcMember.getUser().isBot()) {
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
				System.out.println("Category: " + category.getName());
				System.out.println("Size: " + category.getVoiceChannels().size());
				
				VoiceChannel voiceChannel = (VoiceChannel) category.getVoiceChannels().get(1);
				Date time = Date.from(voiceChannel.getTimeCreated().toInstant());

				Category textChannels = this.instance.getTextChannelCategory(guild);
				TextChannel textChannel = (TextChannel) textChannels.getChannels().get(0);

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);
				if (this.instance.getConfig().getBoolean("Message-Type.Enable-Simple-Mode")) {
					embedBuilder.setAuthor("通話時間: " + this.instance.getUptime(time));

					List<CallTime> list = new ArrayList<>();

					for (CallTime callTime : this.instance.getTimeUtils().getList(guild.getId())) {
						try {
							String tt = callTime.getTime();
							if (!tt.equals("0秒")) {
								Member targetMember = guild.getMemberById(callTime.getName());
								if (targetMember != null) {
									String path = "User." + targetMember.getId() + "." + guild.getId() + ".Vanish";
									boolean isVanish = this.instance.getConfig().getBoolean(path, false);
									if (!isVanish) {
										embedBuilder.setDescription("ユーザーごとの通話時間:");
										embedBuilder.addField(targetMember.getEffectiveName(), callTime.getTime(), true);
									}
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
						list.add(callTime);

						this.instance.getTimeUtils().resetGuild(guild.getId());
					}
				} else {
					embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
					embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true);
					embedBuilder.addField("終了時刻", this.instance.getNowTime(), true);
				}
				
				String path = "Server." + guild.getId() + ".NoSendLogTimeLog";

				if (!this.instance.getConfig().getBoolean(path)) {
					List<TextChannel> general = guild.getTextChannelsByName("general", true);
					if (general != null && general.size() >= 1) {
						textChannel = general.get(0);
					}
					
					textChannel.sendMessageEmbeds(embedBuilder.build()).complete();
				}

			} catch (Exception e) {
				this.instance.postError(e);
			}
			
			String path = "Server." + guild.getId() + ".DisableChannelManagement";
			
			if (!this.instance.getConfig().getBoolean(path)) {
				for (VoiceChannel voiceChannel : voiceChannels) {
					try {
						if (voiceChannel != null && voiceChannel.getType().equals(ChannelType.VOICE)) {
							voiceChannel.delete().complete();
						}
					} catch (Exception e) {
						this.instance.postError(e);
						return;
					}
				}
	
				category.createVoiceChannel(this.instance.getVoiceChannelName(category) + "-1").setUserlimit(99).setBitrate(guild.getMaxBitrate()).complete();
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
