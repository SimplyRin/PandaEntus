package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2021/11/04.
 *
 * Copyright (C) 2021 SimplyRin
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
public class YouTubeNowPlayingCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!nowplaying";
	}

	@Override
	public String getDescription() {
		return "現在再生中の曲を確認";
	}

	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("nowplaying", this.getDescription());
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!np");
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
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		Guild guild = event.getGuild();
		GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);

		AudioTrack audioTrack = musicManager.getPlayer().getPlayingTrack();
		if (audioTrack == null) {
			BaseCommand playCommand = instance.getCommandRegister().getRegisteredCommand(YouTubePlayCommand.class);
			event.reply("現在何も再生していません。\n" + playCommand.getCommand() + " コマンドを利用して音楽を再生することができます。");
			return;
		}

		double position = audioTrack.getPosition() / 1000.0;
		double duration = audioTrack.getDuration() / 1000.0;

		BigDecimal bigDecimal = new BigDecimal((position / duration) * 100).setScale(2, RoundingMode.FLOOR);
		double progress = bigDecimal.doubleValue();
		String percent = progress + "%";

		String bar = "";
		while (bar.length() < 20) {
			bar += "　";
		}

		int barPos = (int) (progress / 5);
		char[] _char = bar.toCharArray();
		_char[barPos] = '●';
		bar = new String(_char);

		System.out.println("▶︎ " + bar + " :" + position + " / " + duration + " = " + percent);

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);

		String prefix = instance.getLoopMap().get(guild.getIdLong()) != null ? "🔁" : "▶";
		if (musicManager.getPlayer().isPaused()) {
			prefix = "⏸";
		}

		embedBuilder.setDescription(prefix + " " + instance.formatMillis(audioTrack.getPosition()) + " / "
				+ instance.formatMillis(audioTrack.getDuration()) + ": ~~" + bar + "~~");
		embedBuilder.addField("🎵 再生中の音楽", audioTrack.getInfo().title, false);
		embedBuilder.addField("💿 アーティスト/チャンネル", audioTrack.getInfo().author, false);
		embedBuilder.addField("🔗 リンク", audioTrack.getInfo().uri, false);
		System.out.println(audioTrack.getInfo().identifier);
		event.reply(embedBuilder.build());
	}

}
