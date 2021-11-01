package net.simplyrin.pandaentus.commands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.httpclient.HttpClient;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.CommandPermission;

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
public class OjichatCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!ojichat";
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

		String name = "";
		for (int i = 1; i < args.length; i++) {
			name = name + args[i] + " ";
		}

		boolean normalText = false;

		name = name.trim();

		// テキトー！！
		if (name.contains("-normal") || name.contains("-n")) {
			normalText = true;
			name = name.replace("-normal", "");
			name = name.replace("-n", "");
		}

		name = name.trim();

		HttpClient httpClient = new HttpClient("https://ojichat.appspot.com/post");
		httpClient.setData("name=" + name + "&emoji_level=4&punctuation_level=0");

		httpClient.setUserAgent(instance.getBotUserAgent());

		httpClient.addHeader("Accept", "application/json, text/plain, */*");
		httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		httpClient.addHeader("Origin", "https://oji.netlify.com");
		httpClient.addHeader("Referer", "https://oji.netlify.com/");

		String result;
		try {
			result = httpClient.getAsJsonObject().get("message").getAsString();
		} catch (Exception e) {
			return;
		}

		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setDescription(result);

		if (normalText) {
			channel.sendMessage(result).complete();
		} else {
			channel.sendMessage(embedBuilder.build()).complete();
		}
		return;
	}

}
