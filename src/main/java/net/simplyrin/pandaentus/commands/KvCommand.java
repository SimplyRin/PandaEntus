package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.io.File;
import java.util.List;
import java.util.UUID;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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
 * Created by SimplyRin on 2020/07/09.
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
public class KvCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!kv";
	}
	
	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandData getCommandData() {
		return null;
	}
	
	@Override
	public List<String> getAlias() {
		return null;
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
			channel.sendMessage(embedBuilder.build()).complete();
			return;
		}

		if (args.length > 1) {
			String uniqueId = "./kv/" + UUID.randomUUID().toString().split("-")[0] + ".wav";
			new File("kv").mkdirs();			
			File file = new File(uniqueId);

			String value = "";
			for (int i = 1; i < args.length; i++) {
				value = value + args[i] + " ";
			}
			value = value.trim();

			String[] command = new String[] { "curl", "https://api.voicetext.jp/v1/tts",
					"-o", uniqueId,
					"-u", instance.getVoiceTextApiKey() + ":",
					"-d", "text=" + value,
					"-d", "speaker=show",
					"-d", "pitch=150",
					"-d", "format=wav" };

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

					VoiceChannel voiceChannel = member.getVoiceState().getChannel();
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
		channel.sendMessage(embedBuilder.build()).complete();
	}

}
