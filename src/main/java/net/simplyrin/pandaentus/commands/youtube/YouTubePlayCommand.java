package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.io.File;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.utils.BaseCommand;
import net.simplyrin.pandaentus.utils.CommandType;
import net.simplyrin.pandaentus.utils.ThreadPool;

/**
 * Created by SimplyRin on 2020/07/09.
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
public class YouTubePlayCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!play";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		if (args.length > 1) {
			String url = args[1];

			if (!(url.contains("youtube.com") || url.contains("youtu.be"))) {
				channel.sendMessage("YouTube のみ対応しています。").complete();
				return;
			}

			if (url.contains("playlist")) {
				channel.sendMessage("現在プレイリストには対応していません。").complete();
				return;
			}

			embedBuilder.setColor(Color.RED);
			embedBuilder.setAuthor("ファイルを準備しています...", null, "https://static.simplyrin.net/gif/loading.gif?id=1");
			Message message = channel.sendMessage(embedBuilder.build()).complete();

			ThreadPool.run(() -> {
				File file = instance.downloadFile(url);
				String videoId = instance.getVideoId(url);

				VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
				AudioManager audioManager = guild.getAudioManager();
				audioManager.openAudioConnection(voiceChannel);
				audioManager.setAutoReconnect(false);

				GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);

				System.out.println("Loading item...");
				instance.getPlayerManager().loadItemOrdered(musicManager, file.getAbsolutePath(), new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						String title = instance.getConfig().getString("YouTube." + videoId + ".Title");
						embedBuilder.setAuthor("次に " + title + " を再生します");
						message.editMessage(embedBuilder.build()).complete();
						instance.getPreviousTrack().put(guild, track);

						ThreadPool.run(() -> {
							String duration = instance.getConfig().getString("YouTube." + videoId + ".Duration");
							int time = instance.durationToTime(duration);

							if (time == 0) {
								System.out.println(title + " time is zero(0)! return.");
								return;
							}

							try {
								TimeUnit.SECONDS.sleep(time);
							} catch (Exception e) {
							}

							if (instance.getLoopMap().get(guild) != null) {
								VoiceChannel vc = guild.getVoiceChannelById(voiceChannel.getId());
								if (vc != null) {
									if (vc.getMembers().size() >= 1) {
										trackLoaded(instance.getLoopMap().get(guild).makeClone());
									}
								}
							}
						});

						instance.play(guild, musicManager, track);
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						AudioTrack firstTrack = playlist.getSelectedTrack();

						if (firstTrack == null) {
							firstTrack = playlist.getTracks().get(0);
						}

						if (instance.getLoopMap().get(guild) == null) {
							instance.play(guild, musicManager, firstTrack);
						}
					}

					@Override
					public void noMatches() {
					}

					@Override
					public void loadFailed(FriendlyException exception) {
						channel.sendMessage("Could not play: " + exception.getMessage()).queue();
					}
				});
			});
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("使用方法: " + args[0] + " <URL>");
		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
