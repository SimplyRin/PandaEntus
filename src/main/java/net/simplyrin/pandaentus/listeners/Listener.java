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
public class Listener extends ListenerAdapter {

	private Main instance;
	private HashMap<String, CallTimeManager> map = new HashMap<>();


	public Listener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		this.instance.getTimeManager().getUser(event.getMember().getUser().getId()).joined();
		this.check(event.getMember(), event.getGuild());

		Guild guild = event.getGuild();
		Member member = event.getMember();

		if (this.map.get(guild.getId()) == null) {
			this.map.put(guild.getId(), new CallTimeManager(this.instance, guild.getId()));
		}

		this.map.get(guild.getId()).join(member.getUser());
	}

	@Override
	public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
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
		this.instance.getTimeManager().getUser(event.getMember().getUser().getId()).quit();

		this.map.get(event.getGuild().getId()).quit(event.getMember().getUser());

		Guild guild = event.getGuild();

		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
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

				Member member = event.getMember();

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);
				if (this.instance.getConfig().getBoolean("Message-Type.Enable-Simple-Mode")) {
					/* try {
						embedBuilder.addField("通話時間", this.instance.getGuildCallManager(guildChannel.getId()).getCurrentTime(), true);
					} catch (Exception e) {
					} */
					embedBuilder.setAuthor("通話時間: " + this.instance.getUptime(time));
					embedBuilder.setDescription("ユーザーごとの通話時間:");

					List<CallTime> list = new ArrayList<>();

					Collection<CallTime> callTimes = this.map.get(event.getGuild().getId()).getMap().values();
					for (CallTime callTime : callTimes) {
						// vanish
						if (this.instance.getConfig().getBoolean("User." + callTime.getUser().getId() + ".Vanish")) {
							continue;
						}
						embedBuilder.addField(this.getNickname(guild.getMember(callTime.getUser())), callTime.getTime().toString(), true);
						list.add(callTime);
					}
				} else {
					embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
					embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true);
					embedBuilder.addField("終了時刻", this.instance.getNowTime(), true);

					/**
					embedBuilder.addField("開始ユーザー", member.getUser().getName(), true);
					if (event.getMember().getNickname() != null) {
						embedBuilder.addField("最終ユーザー", member.getNickname(), true);
					} else {
						embedBuilder.addField("最終ユーザー", member.getUser().getName(), true);
					} */
				}

				this.map.get(event.getGuild().getId()).getMap().clear();

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
