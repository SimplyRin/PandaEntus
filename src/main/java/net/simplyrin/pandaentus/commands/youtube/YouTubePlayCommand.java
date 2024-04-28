package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.simplyrin.pandaentus.utils.ThreadPool;
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
public class YouTubePlayCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!play";
	}
	
	@Override
	public String getDescription() {
		return "曲を追加/再生";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("play", this.getDescription())
				.addOption(OptionType.STRING, "url", "YouTube, Twitch, Bandcamp の URL を入力", true);
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
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[2];
			args[0] = this.getCommand();
			args[1] = s.getOption("url").getAsString();
		}

		Guild guild = event.getGuild();

		if (args.length > 1) {
			String url = args[1];
			
			if (!url.startsWith("http")) {
				event.reply("検索機能を使用することはできません。URL を入力してください。");
				return;
			}
			
			AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();
			if (voiceChannel == null) {
				event.reply("ボイスチャンネルに接続してください。");
				return;
			}
			
			List<String> urls = new ArrayList<>();

			if (url.contains("playlist") || url.contains("list")) {
				
				ProcessManager.runCommand(new String[] { "/usr/local/bin", "--flat-playlist", "--print", "id", url }, new Callback() {
					@Override
					public void line(String response) {
						if (response.length() >= 15) {
							urls.add("https://www.youtube.com/watch?v=" + response);
                        }
					}
				}, false);
				
			
			} else {
				urls.add(url);
			}

			embedBuilder.setColor(Color.RED);
			embedBuilder.setAuthor("ファイルを準備しています...", null, "https://static.simplyrin.net/gif/loading.gif?id=1");
			Message message = event.reply(embedBuilder.build());

			ThreadPool.run(() -> {
				
				AudioManager audioManager = guild.getAudioManager();
				audioManager.openAudioConnection(voiceChannel);
				audioManager.setAutoReconnect(false);

				GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);
				
				List<MessageEmbed> messages = new ArrayList<>();
				
				for (String url_ : urls) {
					instance.getPlayerManager().loadItemOrdered(musicManager, url_, new AudioLoadResultHandler() {
						@Override
						public void trackLoaded(AudioTrack track) {
							embedBuilder.setAuthor(null);
							embedBuilder.clearFields();
							embedBuilder.addField("🎵 タイトル", track.getInfo().title, true);
							embedBuilder.addField("💿 アーティスト", track.getInfo().author, true);
							
							BaseCommand nowPlaying = instance.getCommandRegister().getRegisteredCommand(YouTubeNowPlayingCommand.class);
							BaseCommand yt = instance.getCommandRegister().getRegisteredCommand(YouTubeHelpCommand.class);
							embedBuilder.setFooter("詳細: " + nowPlaying.getCommand() + ", コマンド一覧: " + yt.getCommand());
							messages.add(embedBuilder.build());
							instance.getPreviousTrack().put(guild.getIdLong(), track);
							instance.play(guild, musicManager, track);
							
							if (messages.size() >= urls.size()) {
								MessageEmbed[] l = new MessageEmbed[messages.size()];
								
								for (int i = 0; i < messages.size(); i++) {
									l[i] = messages.get(i);
								}
								
								message.editMessageEmbeds(l).complete();
							}
						}

						@Override
						public void playlistLoaded(AudioPlaylist playlist) {
							instance.setAudioPlaylist(playlist);
							AudioTrack firstTrack = playlist.getSelectedTrack();

							if (firstTrack == null) {
								firstTrack = playlist.getTracks().get(0);
							}

							if (instance.getLoopMap().get(guild.getIdLong()) == null) {
								instance.play(guild, musicManager, firstTrack);
							}
						}

						@Override
						public void noMatches() {
							message.editMessage("曲が見つかりませんでした。").complete();
						}

						@Override
						public void loadFailed(FriendlyException e) {
							message.editMessage("曲を読み込めませんでした。\n" + e.getMessage()).complete();
						}
					});
				}

				
			});
			return;
		}
		
		YouTubeHelpCommand help = (YouTubeHelpCommand) instance.getCommandRegister().getRegisteredCommand(YouTubeHelpCommand.class);

		EmbedBuilder helpEmbed = help.getHelpEmbed(instance);
		helpEmbed.setColor(Color.RED);
		helpEmbed.setAuthor("使用方法: " + args[0] + " <YouTube, Twitch, Bandcamp>");
		helpEmbed.setDescription(help.getHelpMessage());
		
		event.reply(helpEmbed.build());
		return;
	}

}
