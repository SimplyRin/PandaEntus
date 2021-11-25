package net.simplyrin.pandaentus.commands.serveradmin;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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
public class AddVoiceChannelCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!add-vc";
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
		return CommandPermission.ServerAdministrator;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		MessageChannel channel = event.getChannel();
		Category category = instance.getVoiceChannelCategory(event.getGuild());

		if (args.length > 1) {
			int i;
			try {
				i = Integer.valueOf(args[1]).intValue();
			} catch (Exception e) {
				embedBuilder.setColor(Color.RED);
				embedBuilder.setDescription("Invalid usage!");
				channel.sendMessageEmbeds(embedBuilder.build()).complete();
				return;
			}

			List<VoiceChannel> voiceChannels = category.getVoiceChannels();
			int size = voiceChannels.size() + 1;

			int count = 0;
			while (true) {
				if (i == count) {
					break;
				}

				try {
					category.createVoiceChannel("General-" + size).setUserlimit(99).complete();
				} catch (Exception e) {
				}

				if (size == 50) {
					break;
				}

				size++;
				count++;
			}

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("Created!");
			channel.sendMessageEmbeds(embedBuilder.build()).complete();
			return;
		}
	}

}
