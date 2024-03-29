package net.simplyrin.pandaentus.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
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
public class DabCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!dab";
	}
	
	@Override
	public String getDescription() {
		return "<o/ ｲｲﾈ!";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("dab", this.getDescription());
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
		ThreadPool.run(() -> {
			Message message = null;
			String asi = "\n   |\n  /\\";

			int type = 0;
			for (int i = 0; i <= 5; i++) {
				if (message == null) {
					message = event.reply("<o/" + asi);
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

			if (event.getMessage() != null) {
				event.getMessage().delete().complete();
			}

			message.delete().complete();
		});
	}

}
