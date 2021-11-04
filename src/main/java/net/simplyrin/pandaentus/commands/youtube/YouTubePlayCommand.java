package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

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
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.ThreadPool;

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
public class YouTubePlayCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!play";
	}
	
	@Override
	public List<String> getAlias() {
		return Arrays.asList("!p");
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
		Guild guild = event.getGuild();

		if (args.length > 1) {
			String url = args[1];

			if (url.contains("playlist") || url.contains("list")) {
				channel.sendMessage("現在プレイリストには対応していません。").complete();
				return;
			}

			embedBuilder.setColor(Color.RED);
			embedBuilder.setAuthor("ファイルを準備しています...", null, "https://static.simplyrin.net/gif/loading.gif?id=1");
			channel.sendTyping().complete();
			Message message = channel.sendMessage(embedBuilder.build()).complete();
			
			VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
			if (voiceChannel == null) {
				channel.sendMessage("ボイスチャンネルに接続してください。").complete();
				return;
			}

			ThreadPool.run(() -> {
				
				AudioManager audioManager = guild.getAudioManager();
				audioManager.openAudioConnection(voiceChannel);
				audioManager.setAutoReconnect(false);

				GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);

				instance.getPlayerManager().loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
					@Override
					public void trackLoaded(AudioTrack track) {
						embedBuilder.setAuthor(null);
						embedBuilder.clearFields();
						embedBuilder.addField("タイトル", track.getInfo().title, true);
						embedBuilder.addField("アーティスト", track.getInfo().author, true);
						message.editMessage(embedBuilder.build()).complete();
						instance.getPreviousTrack().put(guild, track);
						instance.play(guild, musicManager, track);
					}

					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						instance.setAudioPlaylist(playlist);
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
