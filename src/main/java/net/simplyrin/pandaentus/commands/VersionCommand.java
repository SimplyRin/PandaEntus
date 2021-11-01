package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.util.Scanner;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.utils.Version;

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
public class VersionCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!version";
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
		MessageChannel channel = event.getChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();

		embedBuilder.setColor(Color.GREEN);
		embedBuilder.addField("Version:", Version.BUILD_TIME, true);

		String uptime = "unknown";

		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try {
			process = runtime.exec(new String[] {"uptime", "-p"});
		} catch (Exception e) {
			instance.postError(e);
			return;
		}
		Scanner scanner = new Scanner(process.getInputStream());
		if (scanner.hasNext()) {
			uptime = scanner.nextLine();

			uptime = uptime.replace("up ", "");
			uptime = uptime.replace(",", "");
			uptime = uptime.replace(" years", "年");
			uptime = uptime.replace(" year", "年");

			uptime = uptime.replace(" weeks", "週間");
			uptime = uptime.replace(" week", "週間");
			uptime = uptime.replace(" days", "日");
			uptime = uptime.replace(" day", "日");
			uptime = uptime.replace(" hours", "時間");
			uptime = uptime.replace(" hour", "時間");
			uptime = uptime.replace(" minutes", "分");
			uptime = uptime.replace(" minute", "分");
		}
		scanner.close();
		embedBuilder.addField("Server uptime", uptime, true);

		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
