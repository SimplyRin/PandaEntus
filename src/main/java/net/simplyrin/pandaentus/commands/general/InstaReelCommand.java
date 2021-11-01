package net.simplyrin.pandaentus.commands.general;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.IOUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

/**
 * Created by SimplyRin on 2020/11/25.
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
	public CommandPermission getPermission() {
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
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
