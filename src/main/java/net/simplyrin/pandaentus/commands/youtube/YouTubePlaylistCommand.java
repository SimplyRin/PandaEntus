package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.concurrent.BlockingQueue;

import org.joor.Reflect;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.CommandPermission;

/**
 * Created by SimplyRin on 2020/07/11.
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
		BlockingQueue<AudioTrack> queue = Reflect.on(musicManager.scheduler).field("queue").get();

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
