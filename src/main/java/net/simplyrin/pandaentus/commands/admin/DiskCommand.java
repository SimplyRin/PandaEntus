package net.simplyrin.pandaentus.commands.admin;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

/**
 * Created by SimplyRin on 2020/07/09.
 *
 * Copyright (c) 2020 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class DiskCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!disk";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public Permission getPermission() {
		return Permission.Administrator;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		ProcessManager.runCommand(new String[] { "df", "-h", "/" }, new Callback() {
			int i = 0;

			String size, used, available, usePercent;
			Color color;

			@Override
			public void line(String line) {
				System.out.println(line);

				if (this.i == 1) {
					String[] args = line.trim().replaceAll(" +", " ").split(" ");
					int i = 0;
					for (String arg : args) {
						System.out.println("args[" + i + "] -> " + arg);
						i++;
					}
					this.size = args[1];
					this.used = args[2];
					this.available = args[3];
					this.usePercent = args[4];

					int percent = Integer.valueOf(this.usePercent.replace("%", "").trim());
					if (percent >= 80) {
						color = Color.RED;
					} else if (percent >= 60) {
						color = Color.YELLOW;
					} else if (percent >= 0) {
						color = Color.GREEN;
					}
				}

				if (this.i == 0) {
					this.i = 1;
				}
			}

			@Override
			public void processEnded() {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(color);
				embedBuilder.setAuthor("Disk usage (" + this.usePercent + "/100%)");
				embedBuilder.addField("Size", this.size, true);
				embedBuilder.addField("Used", this.used, true);
				embedBuilder.addField("Free", this.available, true);
				channel.sendMessage(embedBuilder.build()).complete();
			}
		}, true);
	}

}
