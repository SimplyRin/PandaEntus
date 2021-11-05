package net.simplyrin.pandaentus.commands.youtube;

import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

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
public class YouTubeResumeCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!resume";
	}

	@Override
	public String getDescription() {
		return "一時停止してる曲を再生";
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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);
		
		VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
		if (voiceChannel == null) {
			channel.sendMessage("ボイスチャンネルに接続してください。").complete();
			return;
		}
		
		AudioTrack audioTrack = musicManager.getPlayer().getPlayingTrack();
		if (audioTrack == null) {
			BaseCommand playCommand = instance.getCommandRegister().getRegisteredCommand(YouTubePlayCommand.class);
			channel.sendMessage("現在何も再生していません。\n" + playCommand.getCommand() + " コマンドを利用して音楽を再生することができます。").complete();
			return;
		}
		
		musicManager.getPlayer().setPaused(false);
		channel.sendMessage("一時停止している曲を再生します。").complete();
	}

}
