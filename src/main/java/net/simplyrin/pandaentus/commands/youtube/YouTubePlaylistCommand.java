package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

/**
 * Created by SimplyRin on 2020/07/11.
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
public class YouTubePlaylistCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!playlist";
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

		MessageChannel channel = event.getChannel();

		GuildMusicManager musicManager = instance.getGuildAudioPlayer(event.getGuild());
		BlockingQueue<AudioTrack> queue = musicManager.scheduler.queue;

		if (queue == null || queue.isEmpty()) {
			channel.sendMessage("次に再生が予定されている曲はありません。\n"
					+ "!play を使用することで、プレイリストに追加することができます。").complete();
			return;
		}

		String message = "";

		AudioTrack playingTrack = musicManager.player.getPlayingTrack();
		if (playingTrack != null) {
			embedBuilder.setAuthor("🎵 再生中の音楽: " + playingTrack.getInfo().title);
		}

		int i = 1;
		for (AudioTrack audioTrack : queue) {
			message += "**" + i + ":** __" + audioTrack.getInfo().title + "__\n";
			i++;
		}

		if (message.length() == 0) {
			channel.sendMessage("再生待ちの曲はありません。").complete();
			return;
		}
		if (message.length() >= 1800) {
			message = message.substring(0, 1800);
		}
		embedBuilder.setDescription(message);
		embedBuilder.setColor(Color.CYAN);
		embedBuilder.setFooter("追加: !play, スキップ: !skip");
		channel.sendMessage(embedBuilder.build()).complete();
	}

}
