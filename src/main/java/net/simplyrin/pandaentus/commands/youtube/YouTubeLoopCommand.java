package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class YouTubeLoopCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!loop";
	}
	
	@Override
	public String getDescription() {
		return "再生中の曲をループ";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("loop", this.getDescription());
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

		Guild guild = event.getGuild();

		if (instance.getLoopMap().get(guild.getIdLong()) != null) {
			embedBuilder.setColor(Color.RED);
			embedBuilder.setDescription("🔁 ループ再生を無効にしました。");
			instance.getLoopMap().remove(guild.getIdLong());
			instance.getPreviousTrack().remove(guild.getIdLong());
		} else {
			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("🔁 ループ再生を有効にしました。");
			instance.getLoopMap().put(guild.getIdLong(), instance.getPreviousTrack().get(guild.getIdLong()));
		}

		event.reply(embedBuilder.build());
	}

}
