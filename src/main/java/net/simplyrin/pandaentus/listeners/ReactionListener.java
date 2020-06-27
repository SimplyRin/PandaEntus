package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.io.File;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.Main.Callback;

/**
 * Created by SimplyRin on 2020/06/21.
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
public class ReactionListener extends ListenerAdapter {

	private Main instance;

	public ReactionListener(Main instance) {
		this.instance = instance;
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser().isBot() || event.getUser().isFake()) {
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

		String url = message.getContentRaw();

		String tempVideoId = url.replace("https://www.youtube.com/watch?v=", "");
		if (tempVideoId.contains("&")) {
			tempVideoId = tempVideoId.split("&")[0];
		}
		tempVideoId = tempVideoId.replace("https://youtu.be/", "");
		tempVideoId = tempVideoId.replace("http://youtu.be/", "");
		tempVideoId = tempVideoId.replace("http://www.youtube.com/watch?v=", "");
		final String videoId = tempVideoId;

		final File file = new File("ytdl");
		file.mkdirs();

		final File mp3 = new File(file, videoId + ".mp3");
		final MessageChannel channel = message.getChannel();

		final User user = event.getUser();

		if (mp3.exists()) {
			new Thread(() -> {
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
			}).start();
			return;
		}

		String loadingUrl = "https://static.simplyrin.net/gif/loading.gif";
		String downloadingUrl = "https://static.simplyrin.net/gif/download.gif";
		String uploadingUrl = "https://static.simplyrin.net/gif/upload-cat.gif";

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.GREEN);
		embedBuilder.setAuthor(user.getName() + " -> 処理中...", null, loadingUrl);
		Message phase = channel.sendMessage(embedBuilder.build()).complete();

		this.instance.runCommand(new String[]{ "youtube-dl", "--get-title", videoId }, new Callback() {
			@Override
			public void response(String response) {
				if (!response.toLowerCase().startsWith("error:")) {
					System.out.println("Title: " + response);
					instance.getConfig().set("YouTube." + videoId + ".Title", response);
				}
			}

			@Override
			public void processEnded() {
			}
		});

		this.instance.runCommand(new String[]{ "youtube-dl", "--get-duration", videoId }, new Callback() {
			@Override
			public void response(String response) {
				if (response.contains(":")) {
					System.out.println("Duration: " + response);
					instance.getConfig().set("YouTube." + videoId + ".Duration", response);
				}
				boolean ok = false;
				int ll = response.split(":").length;
				if (ll == 2) {
					int length = Integer.valueOf(response.split(":")[0]);
					if (length <= 6) {
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
				instance.runCommand(new String[] { "youtube-dl", videoId }, new Callback() {
					private File file;
					@Override
					public void response(String response) {
						if (response.startsWith("[ffmpeg] Merging formats into")) {
							String title = response.replace("[ffmpeg] Merging formats into", "").replace("\"", "").trim();
							System.out.println("Title: " + title);
							this.file = new File(title);
						}
						if (this.file != null) {
							embedBuilder.setAuthor(user.getName() + " -> ダウンロード完了。変換中...", null, loadingUrl);
							phase.editMessage(embedBuilder.build()).complete();
						}
					}

					@Override
					public void processEnded() {
						instance.runCommand(new String[] { "ffmpeg", "-i", this.file.getAbsolutePath(), mp3.getAbsolutePath() }, new Callback() {
							@Override
							public void response(String response) {
								System.out.println(response);
							}

							@Override
							public void processEnded() {
								instance.getConfig().set("YouTube." + videoId + ".Path", mp3.getAbsolutePath());
								embedBuilder.setAuthor(user.getName() + " -> 変換完了。送信準備を行っています...", null, uploadingUrl);
								phase.editMessage(embedBuilder.build()).complete();
								System.out.println(mp3.exists());
								if (mp3.exists()) {
									EmbedBuilder embedBuilder = new EmbedBuilder();
									embedBuilder.setColor(Color.RED);
									embedBuilder.setAuthor("ファイルが準備できました。");
									embedBuilder.addField("タイトル", instance.getConfig().getString("YouTube." + videoId + ".Title"), true);
									embedBuilder.addField("長さ", instance.getConfig().getString("YouTube." + videoId + ".Duration"), true);
									Message message = channel.sendFile(mp3).embed(embedBuilder.build()).complete();
									phase.delete().complete();
									file.delete();

									try {
										Thread.sleep(1000 * 60 * 60 * 48);
									} catch (Exception e) {
									}

									message.delete().complete();
								}
							}
						});
					}
				});
			}

			@Override
			public void processEnded() {
			}
		});
		return;
	}

}
