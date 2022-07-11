package net.simplyrin.pandaentus.commands.youtube;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class YouTubeDisconnectCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!disconnect";
	}

	@Override
	public String getDescription() {
		return "Bot を VC から切断";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("disconnect", this.getDescription());
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
		Guild guild = event.getGuild();
		
		AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();
		if (voiceChannel == null) {
			event.reply("ボイスチャンネルに接続してください。");
			return;
		}

		instance.getGuildAudioPlayer(guild).getPlayer().destroy();
		guild.getAudioManager().closeAudioConnection();
		
		event.reply("切断しました。");
	}

}
