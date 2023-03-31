package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.io.File;
import java.util.UUID;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

/**
 * Created by SimplyRin on 2023/03/30.
 *
 * Copyright (c) 2023 SimplyRin
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
public class HvCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!hv";
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
		EmbedBuilder embedBuilder = new EmbedBuilder();

		MessageChannel channel = event.getChannel();
		Member member = event.getMember();
		Guild guild = event.getGuild();

		if (member.getVoiceState() == null && member.getVoiceState().getChannel() == null) {
			embedBuilder.setColor(Color.RED);
			embedBuilder.setDescription("ボイスチャンネルに接続してください！");
			channel.sendMessageEmbeds(embedBuilder.build()).complete();
			return;
		}

		if (args.length > 1) {
			String uniqueId = "./hv/" + UUID.randomUUID().toString().split("-")[0] + ".wav";
			new File("kv").mkdirs();			
			File file = new File(uniqueId);

			String value = "";
			for (int i = 1; i < args.length; i++) {
				value = value + args[i] + " ";
			}
			value = value.trim();

			String[] command = new String[] { "tarakotalk", "save",
					"\"" + value + "\"", uniqueId};

			event.getMessage().delete().complete();

			ProcessManager.runCommand(command, new Callback() {
				@Override
				public void line(String response) {
					System.out.println(response);
				}

				@Override
				public void processEnded() {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}

					AudioChannel voiceChannel = member.getVoiceState().getChannel();
					AudioManager audioManager = guild.getAudioManager();
					audioManager.openAudioConnection(voiceChannel);
					audioManager.setAutoReconnect(false);

					GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);
					file.deleteOnExit();
					
					instance.getPlayerManager().loadItemOrdered(musicManager, file.getAbsolutePath(), new AudioLoadResultHandler() {
						@Override
						public void trackLoaded(AudioTrack track) {
							instance.play(guild, musicManager, track);
						}

						@Override
						public void playlistLoaded(AudioPlaylist playlist) {
							AudioTrack firstTrack = playlist.getSelectedTrack();

							if (firstTrack == null) {
								firstTrack = playlist.getTracks().get(0);
							}

							instance.play(guild, musicManager, firstTrack);
						}

						@Override
						public void noMatches() {
						}

						@Override
						public void loadFailed(FriendlyException exception) {
							channel.sendMessage("Could not play: " + exception.getMessage()).queue();
						}
					});
				}
			}, true);
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("使用方法: " + args[0] + " <ボイス>");
		channel.sendMessageEmbeds(embedBuilder.build()).complete();
	}

}
