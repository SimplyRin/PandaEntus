package net.simplyrin.pandaentus.commands.botowner;

import java.awt.Color;
import java.io.File;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
public class DiskCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!disk";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.BotOwner;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		File file = new File(".");
    	long totalSpace = file.getTotalSpace();
    	long usableSpace = file.getUsableSpace();
    	long usedSpace = totalSpace - usableSpace;

    	int percent = (int) (usedSpace * 100 / totalSpace);
    	Color color = null;
    	if (percent >= 80) {
			color = Color.RED;
		} else if (percent >= 60) {
			color = Color.YELLOW;
		} else if (percent >= 0) {
			color = Color.GREEN;
		}

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(color);
		embedBuilder.setAuthor("Disk usage (" + percent + "/100%)");
		embedBuilder.addField("Size", instance.formatSize(totalSpace), true);
		embedBuilder.addField("Used", instance.formatSize(usedSpace), true);
		embedBuilder.addField("Free", instance.formatSize(usableSpace), true);
		channel.sendMessageEmbeds(embedBuilder.build()).complete();
	}

}
