package net.simplyrin.pandaentus.commands.youtube;

import java.util.List;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class YouTubeVolumeCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!volume";
	}
	
	@Override
	public String getDescription() {
		return "ボリュームを調節";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("volume", this.getDescription())
				.addOption(OptionType.INTEGER, "音量", "1-100 で音量を調節", true);
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
		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[s.getOptions().size() + 1];
			args[0] = this.getCommand();
			args[1] = s.getOption("音量").getAsString();
		}
		
		if (args.length > 1) {
			try {
				int volume = Integer.valueOf(args[1]);
				if (volume >= 1 && volume <= 100) {
					instance.getConfig().set("Guild." + event.getGuild().getId() + ".Voice-Volume", volume);
					GuildMusicManager musicManager = instance.getGuildAudioPlayer(event.getGuild());
					musicManager.getPlayer().setVolume(volume);
					event.reply("ボリュームを " + volume + " に変更しました。");
					return;
				}
			} catch (Exception e) {
			}
		}

		event.reply("使用方法: " + this.getCommand() + " <1-100>");
	}

}
