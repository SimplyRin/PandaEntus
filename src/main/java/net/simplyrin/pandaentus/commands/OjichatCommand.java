package net.simplyrin.pandaentus.commands;

import java.awt.Color;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.httpclient.HttpClient;
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
public class OjichatCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!ojichat";
	}
	
	@Override
	public String getDescription() {
		return "おじさん構文を生成";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("ojichat", this.getDescription())
				.addOption(OptionType.STRING, "名前", "指定した名前でおじさん構文を生成します。");
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
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[2];
			args[0] = this.getCommand();
			args[1] = s.getOption("名前") != null ? s.getOption("名前").getAsString() : "";
		}

		MessageChannel channel = event.getChannel();

		String name = "";
		for (int i = 1; i < args.length; i++) {
			name += args[i] + " ";
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

		if (event.isSlashCommand()) {
			event.reply(embedBuilder.build());
		} else {
			if (normalText) {
				channel.sendMessage(result).complete();
			} else {
				channel.sendMessageEmbeds(embedBuilder.build()).complete();
			}
		}
		return;
	}

}
