package net.simplyrin.pandaentus.commands.youtube;

import java.util.concurrent.BlockingQueue;

import org.joor.Reflect;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.utils.BaseCommand;
import net.simplyrin.pandaentus.utils.CommandType;

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
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		GuildMusicManager musicManager = instance.getGuildAudioPlayer(event.getGuild());
		BlockingQueue<AudioTrack> queue = Reflect.on(musicManager.scheduler).field("queue").get();

		if (queue == null || queue.isEmpty()) {
			channel.sendMessage("プレイリストが正しく読み込まれていません。").complete();
			return;
		}

		int i = 1;
		String message = "";

		for (AudioTrack audioTrack : queue) {
			String title = "";
			for (String key : instance.getConfig().getSection("YouTube").getKeys()) {
				String path = instance.getConfig().getString("YouTube." + key + ".Path");
				if (path.equals(audioTrack.getIdentifier())) {
					title = instance.getConfig().getString("YouTube." + key + ".Title");
				}
			}
			message += i + ": " + title + "\n";
		}

		if (message.length() == 0) {
			channel.sendMessage("再生街の曲はありません。").complete();
			return;
		}
		if (message.length() >= 1800) {
			message = message.substring(0, 1800);
		}
		channel.sendMessage(message).complete();
	}

}
