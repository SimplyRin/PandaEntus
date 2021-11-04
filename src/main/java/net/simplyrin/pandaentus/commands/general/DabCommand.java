package net.simplyrin.pandaentus.commands.general;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.ThreadPool;

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
public class DabCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!dab";
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
		ThreadPool.run(() -> {
			MessageChannel channel = event.getChannel();
			Message message = null;
			String asi = "\n   |\n  /\\";

			int type = 0;
			for (int i = 0; i <= 5; i++) {
				if (message == null) {
					message = channel.sendMessage("<o/" + asi).complete();
				} else {
					if (type == 1) {
						type = 0;
						message.editMessage("<o/" + asi).complete();
					} else if (type == 0) {
						type = 1;
						message.editMessage("\\o>" + asi).complete();
					}
				}

				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
				}
			}

			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			event.getMessage().delete().complete();
			message.delete().complete();
		});
	}

}
