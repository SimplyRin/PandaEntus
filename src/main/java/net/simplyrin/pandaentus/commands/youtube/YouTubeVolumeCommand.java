package net.simplyrin.pandaentus.commands.youtube;

import java.util.List;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

/**
 * Created by SimplyRin on 2020/07/17.
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
public class YouTubeVolumeCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!volume";
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

		if (args.length > 1) {
			try {
				int volume = Integer.valueOf(args[1]);
				if (volume >= 20 && volume <= 100) {
					instance.getConfig().set("Guild." + event.getGuild().getId() + ".Voice-Volume", volume);
					GuildMusicManager musicManager = instance.getGuildAudioPlayer(event.getGuild());
					musicManager.player.setVolume(volume);
					channel.sendMessage("ボリュームを " + volume + " に変更しました。").complete();
					return;
				}
			} catch (Exception e) {
			}
		}

		channel.sendMessage("使用方法: " + this.getCommand() + " <20-100>").complete();
	}

}
