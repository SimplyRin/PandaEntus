package net.simplyrin.pandaentus.commands.youtube;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.ReactionMessage;

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
public class YouTubeCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "https://www.youtube.com/watch?v=";
	}
	
	@Override
	public List<String> getAlias() {
		return Arrays.asList("https://youtu.be/");
	}

	@Override
	public CommandType getType() {
		return CommandType.StartsWith;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		Emote emote = null;
		for (Emote temp : event.getGuild().getEmotes()) {
			if (emote == null) {
				emote = temp;
			}
			if (temp.getName().equals("download")) {
				emote = temp;
			}
		}
		event.getMessage().addReaction(emote).complete();
		ReactionMessage rm = new ReactionMessage(event.getMessage(), emote);
		instance.getMessages().add(rm);
	}

}
