package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.io.File;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.utils.ThreadPool;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

/**
 * Created by SimplyRin on 2020/06/21.
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
public class ReactionListener extends ListenerAdapter {

	private PandaEntus instance;

	public ReactionListener(PandaEntus instance) {
		this.instance = instance;
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}

		Message message = null;
		for (Message temp : this.instance.getMessages()) {
			if (temp.getId().equals(event.getMessageId())) {
				message = temp;
			}
		}
		if (message == null) {
			return;
		}

		this.instance.getMessages().remove(message);

		final MessageChannel channel = message.getChannel();

		String url = message.getContentRaw();

		String tempVideoId = url;
		
		if (tempVideoId.contains("v=")) {
			tempVideoId = tempVideoId.split("v=")[1];
			tempVideoId = tempVideoId.split("&")[0];
		}
		
		tempVideoId = tempVideoId.replace("https://www.youtube.com/watch?v=", "");
		tempVideoId = tempVideoId.replace("https://youtu.be/", "");
		tempVideoId = tempVideoId.replace("http://youtu.be/", "");
		tempVideoId = tempVideoId.replace("http://www.youtube.com/watch?v=", "");
		
		tempVideoId = tempVideoId.split("&")[0];
		tempVideoId = tempVideoId.split("[?]")[0];
		
		final String videoId = tempVideoId;
		
		System.out.println("Original: " + url + ", Repalced: " + videoId);

		final File file = new File("ytdl");
		file.mkdirs();

		final File mp3 = new File(file, videoId + ".mp3");

		final User user = event.getUser();

		if (mp3.exists()) {
			ThreadPool.run(() -> {
				EmbedBuilder embedBuilder = new EmbedBuilder();
				embedBuilder.setColor(Color.GREEN);
				embedBuilder.setAuthor("ファイルが準備できました。");
				embedBuilder.addField("タイトル", instance.getConfig().getString("YouTube." + videoId + ".Title"), true);
				embedBuilder.addField("長さ", instance.getConfig().getString("YouTube." + videoId + ".Duration"), true);
				Message tempMessage = channel.sendFile(mp3).embed(embedBuilder.build()).complete();

				try {
					Thread.sleep(1000 * 60 * 60 * 48);
				} catch (Exception e) {
				}

				tempMessage.delete().complete();
			});
			return;
		}

		String loadingUrl = "https://static.simplyrin.net/gif/loading.gif?id=1";
		String downloadingUrl = "https://static.simplyrin.net/gif/download.gif?id=1";
		String uploadingUrl = "https://static.simplyrin.net/gif/upload.gif?id=1";

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setAuthor(user.getName() + " -> 処理中...", null, loadingUrl);
		Message phase = channel.sendMessage(embedBuilder.build()).complete();
		
		String youtubeDlPathTemp = "youtube-dl";
		File youtubeDl = new File("youtube-dl");
		if (youtubeDl.exists()) {
			youtubeDlPathTemp = youtubeDl.getAbsolutePath();
		}
		
		final String youtubeDlPath = youtubeDlPathTemp;

		ProcessManager.runCommand(new String[]{ youtubeDlPath, "--get-title", videoId }, new Callback() {
			@Override
			public void line(String response) {
				if (!response.toLowerCase().startsWith("error:")) {
					System.out.println("Title: " + response);
					instance.getConfig().set("YouTube." + videoId + ".Title", response);
				}
			}
		}, true);

		ProcessManager.runCommand(new String[]{ youtubeDlPath, "--get-duration", videoId }, new Callback() {
			@Override
			public void line(String response) {
				if (response.contains(":")) {
					System.out.println("Duration: " + response);
					instance.getConfig().set("YouTube." + videoId + ".Duration", response);
				}
				boolean ok = false;
				int ll = response.split(":").length;
				if (ll == 2) {
					int length = Integer.valueOf(response.split(":")[0]);
					if (length <= 5) {
						ok = true;
					}
				}

				if (!ok) {
					embedBuilder.setColor(Color.RED);
					embedBuilder.setAuthor(user.getName() + " -> 動画が長すぎます！", null, loadingUrl);
					phase.editMessage(embedBuilder.build()).complete();
					return;
				}

				embedBuilder.setAuthor(user.getName() + " -> ダウンロードしています...", null, downloadingUrl);
				phase.editMessage(embedBuilder.build()).complete();
				
				
				ProcessManager.runCommand(new String[] { youtubeDlPath, "--audio-format", "mp3", "--output", "./ytdl/" + videoId + ".%(ext)s", "-x", videoId }, new Callback() {
					@Override
					public void line(String response) {
						if (response.startsWith("[ffmpeg] Destination:")) {
							String title = response.replace("[ffmpeg] Destination:", "").replace("\"", "").trim();
							System.out.println("Filename: " + title);
						}
					}

					@Override
					public void processEnded() {
						instance.getConfig().set("YouTube." + videoId + ".Path", mp3.getAbsolutePath());
						embedBuilder.setAuthor(user.getName() + " -> 送信しています...", null, uploadingUrl);
						phase.editMessage(embedBuilder.build()).complete();
						if (mp3.exists()) {
							EmbedBuilder embedBuilder = new EmbedBuilder();
							embedBuilder.setColor(Color.GREEN);
							embedBuilder.setAuthor("ファイルが準備できました。");
							
							embedBuilder.addField("タイトル", instance.getConfig().getString("YouTube." + videoId + ".Title"), true);
							embedBuilder.addField("長さ", instance.getConfig().getString("YouTube." + videoId + ".Duration"), true);
							channel.sendFile(mp3).embed(embedBuilder.build()).complete();
							phase.delete().complete();

						}
					}
				}, true);
			}
		}, true);
		return;
	}

}
