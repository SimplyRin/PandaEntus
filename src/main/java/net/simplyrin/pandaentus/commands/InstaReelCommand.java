package net.simplyrin.pandaentus.commands;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;

/**
 * Created by SimplyRin on 2020/11/25.
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
public class InstaReelCommand implements BaseCommand {

	public static void main(String[] args) {
		new InstaReelCommand().execute(null, null, new String[] { "https://www.instagram.com/reel/..." });
	}

	@Override
	public String getCommand() {
		return "https://www.instagram.com/reel/";
	}

	@Override
	public CommandType getType() {
		return CommandType.StartsWith;
	}

	@Override
	public Permission getPermission() {
		return Permission.Everyone;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		String url = args[0];
		if (url.contains(Pattern.quote("?"))) {
			url = url.split(Pattern.quote("?"))[0];
		}

		System.out.println("[InstaReelCommand] Checking: " + url);

		HttpsURLConnection connection = null;
		try {
			connection = (HttpsURLConnection) new URL(url).openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		connection.addRequestProperty("accept", "*/*");
		connection.addRequestProperty("accept-encoding", "identity;q=1, *;q=0");
		connection.addRequestProperty("accept-language", "ja-JP,ja;q=0.9,en-US;q=0.8,en;q=0.7");
		connection.addRequestProperty("cache-control", "no-cache");
		connection.addRequestProperty("pragma", "no-cache");
		connection.addRequestProperty("range", "bytes=0-");
		connection.addRequestProperty("referer", "https://www.instagram.com/");
		connection.addRequestProperty("sec-fetch-dest", "video");
		connection.addRequestProperty("sec-fetch-dest", "no-cors");
		connection.addRequestProperty("sec-fetch-site", "cross-site");
		connection.addRequestProperty("user-agent", instance.getBotUserAgent());

		String result = null;
		try {
			connection.connect();
			result = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result != null && result.contains("property=\"og:video\" content=\"")) {
			String videoUrl = result.split(Pattern.quote("property=\"og:video\" content=\""))[1].split(Pattern.quote("/>"))[0];
			videoUrl = videoUrl.replace("\"", "");
			videoUrl = videoUrl.trim();

			System.out.println(videoUrl);
			event.getChannel().sendMessage(videoUrl).complete();
		} else {
			System.out.println("検出できませんでした。");
		}
	}

}
