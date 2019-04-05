package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
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
import net.simplyrin.pandaentus.utils.GuildCallManager;

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

	public Listener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		this.instance.getTimeManager().getUser(event.getMember().getUser().getId()).joined();
		this.check(event.getMember(), event.getGuild());
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
		for (VoiceChannel voiceChannel : voiceChannels) {
			if (voiceChannel.getMembers().size() >= 1) {
				max[current] = true;
			}

			current++;
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

		Guild guild = event.getGuild();

		Category category = guild.getCategoriesByName("Voice Channels", true).get(0);
		List<VoiceChannel> voiceChannels = category.getVoiceChannels();

		if (voiceChannels.size() == 1) {
			return;
		}

		boolean[] free = new boolean[voiceChannels.size()];
		int current = 0;
		for (VoiceChannel voiceChannel : voiceChannels) {
			if (voiceChannel.getMembers().size() == 0) {
				free[current] = true;
			}

			current++;
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

				GuildCallManager guildCallManager = this.instance.getGuildCallManager(guildChannel.getId());
				Member member = guild.getMemberById(guildCallManager.getJoinUserId());

				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);


				if (this.instance.getConfig().getBoolean("Message-Type.Enable-Simple-Mode")) {
					embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
				} else {
					embedBuilder.addField("通話時間", this.instance.getUptime(time), true);
					embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true);
					embedBuilder.addField("終了時刻", this.instance.getNowTime(), true);

					embedBuilder.addField("開始ユーザー", member.getUser().getName(), true);
					if (event.getMember().getNickname() != null) {
						embedBuilder.addField("最終ユーザー", event.getMember().getNickname(), true);
					} else {
						embedBuilder.addField("最終ユーザー", event.getMember().getUser().getName(), true);
					}
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
				voiceChannel.delete().complete();
			}

			category.createVoiceChannel("General-1").complete().getManager().setUserLimit(99).complete();
		}
	}

}
