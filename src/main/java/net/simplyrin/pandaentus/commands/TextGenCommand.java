package net.simplyrin.pandaentus.commands;

import java.awt.Color;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
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
public class TextGenCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!textgen";
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
		MessageChannel channel = event.getChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		if (args.length > 1) {
			String text = args[1];
			channel.sendTyping().complete();

			HttpClient httpClient = new HttpClient("https://ja.cooltext.com/PostChange");

			// httpClient.addHeader("cookie", "_ga=GA1.2.1279499266.1558787537; _gid=GA1.2.1065377376.1558787537; ASP.NET_SessionId=" + UUID.randomUUID().toString().split("-")[0]);
			httpClient.addHeader("origin", "https://ja.cooltext.com");
			httpClient.addHeader("accept-encoding", "gzip, deflate, br");
			httpClient.addHeader("accept-language", "ja-JP,ja;q=0.9,en;q=0.8,zh-CN;q=0.7,zh;q=0.6");
			httpClient.addHeader("user-agent", instance.getBotUserAgent());
			httpClient.addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8");
			httpClient.addHeader("accept", "*/*");
			httpClient.addHeader("referer", "https://ja.cooltext.com/Logo-Design-Particle");
			httpClient.addHeader("authority", "ja.cooltext.com");
			httpClient.addHeader("x-requested-with", "XMLHttpRequest");

			// httpClient.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
			httpClient.setData("LogoID=39&Text=" + text + "&FontSize=70&Color1_color=23320000&Integer5=3&Integer7=0&Integer8=0&Integer14_color=#23000000^&Integer6=95&Integer9=0&Integer13=on&Integer12=on&FileFormat=6&BackgroundColor_color=#23FFFFFF");
			JsonObject result = httpClient.getAsJsonObject();
			System.out.println("Result: " + result);

			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("Image generated!");
			embedBuilder.setImage(result.get("renderLocation").getAsString());
			
			event.reply(embedBuilder.build());
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("使用方法: " + args[0] + " <テキスト>");
		
		event.reply(embedBuilder.build());
		return;
	}

}
