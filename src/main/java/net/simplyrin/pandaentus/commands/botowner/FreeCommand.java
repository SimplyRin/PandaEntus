package net.simplyrin.pandaentus.commands.botowner;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.CommandPermission;
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
public class FreeCommand implements BaseCommand {

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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
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
				channel.sendMessage(embedBuilder.build()).complete();
			}
		}, true);
	}

}
