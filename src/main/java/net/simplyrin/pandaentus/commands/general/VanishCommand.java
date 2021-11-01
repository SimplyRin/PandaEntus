package net.simplyrin.pandaentus.commands.general;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

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
public class VanishCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!vanish";
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
		User user = event.getAuthor();
		if (args.length > 1 && instance.isBotOwner(user)) {
			String id = args[1];
			id = id.replace("<", "");
			id = id.replace(">", "");
			id = id.replace("!", "");
			id = id.replace("@", "");

			Member member = event.getGuild().getMemberById(id);
			if (member != null) {
				user = member.getUser();
			}
		}

		Configuration config = instance.getConfig();

		String path = "User." + user.getId() + "." + event.getGuild().getId() + ".Vanish";

		boolean bool = config.getBoolean(path);
		bool = !bool;

		instance.getConfig().set(path, bool);

		embedBuilder.setColor(Color.GRAY);
		embedBuilder.setDescription("You are now " + (bool ? "vanished" : "unvanished") + ".");

		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
