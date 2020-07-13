package net.simplyrin.pandaentus.commands;

import java.awt.Color;

import com.google.gson.JsonObject;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.httpclient.HttpClient;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;

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
public class TextGenCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!textgen";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public Permission getPermission() {
		return Permission.Everyone;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
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
			httpClient.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
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
			channel.sendMessage(embedBuilder.build()).complete();
			return;
		}

		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("使用方法: " + args[0] + " <テキスト>");
		channel.sendMessage(embedBuilder.build()).complete();
		return;
	}

}
