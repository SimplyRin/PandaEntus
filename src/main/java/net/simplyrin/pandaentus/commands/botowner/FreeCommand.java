package net.simplyrin.pandaentus.commands.botowner;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

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
public class FreeCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!free";
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

		ProcessManager.runCommand(new String[] { "free", "-h" }, new Callback() {
			String memTotal, memUsed, memFree, swapTotal, swapUsed, swapFree;

			@Override
			public void line(String line) {
				System.out.println(line);

				if (line.startsWith("Mem:")) {
					String[] args = line.trim().replaceAll(" +", " ").split(" ");
					int i = 0;
					for (String arg : args) {
						System.out.println("args[" + i + "] -> " + arg);
						i++;
					}
					this.memTotal = args[1];
					this.memUsed = args[2];
					this.memFree = args[3];
				}

				else if (line.startsWith("Swap")) {
					String[] args = line.trim().replaceAll(" +", " ").split(" ");
					int i = 0;
					for (String arg : args) {
						System.out.println("args[" + i + "] -> " + arg);
						i++;
					}
					this.swapTotal = args[1];
					this.swapUsed = args[2];
					this.swapFree = args[3];
				}
			}

			@Override
			public void processEnded() {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.YELLOW);
				embedBuilder.setAuthor("Ram usage");
				embedBuilder.addField("Total", this.memTotal, true);
				embedBuilder.addField("Used", this.memUsed, true);
				embedBuilder.addField("Free", this.memFree, true);
				embedBuilder.addField("Swap Total", this.swapTotal, true);
				embedBuilder.addField("Swap Used", this.swapUsed, true);
				embedBuilder.addField("Swap Free", this.swapFree, true);
				channel.sendMessageEmbeds(embedBuilder.build()).complete();
			}
		}, true);
	}

}
