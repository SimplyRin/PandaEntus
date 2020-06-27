package net.simplyrin.pandaentus.listeners;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.UUID;

import org.codehaus.plexus.util.FileUtils;

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

		EmbedBuilder embedBuilder = new EmbedBuilder();

		if (mp3.exists()) {
			new Thread(() -> {
				embedBuilder.setDescription("ファイルが準備できました。\nファイルは48時間後削除されます。");
				embedBuilder.setColor(Color.RED);
				embedBuilder.addField("タイトル", this.instance.getConfig().getString("YouTube." + videoId + ".Title"), true);
				embedBuilder.addField("長さ", this.instance.getConfig().getString("YouTube." + videoId + ".Duration"), true);
				Message tempMessage = channel.sendFile(mp3).embed(embedBuilder.build()).complete();

				try {
					Thread.sleep(1000 * 60 * 60 * 48);
				} catch (Exception e) {
				}

				tempMessage.delete().complete();
			}).start();
			return;
		}

		Message phase = channel.sendMessage(user.getAsMention() + " -> 処理中...").complete();

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
					phase.editMessage(user.getAsMention() + " -> 動画が長すぎます！").complete();
					return;
				}

				instance.runCommand(new String[]{ "youtube-dl", "-x", "--audio-format", "m4a", "-g", videoId } , new Callback() {
					@Override
					public void response(String response) {
						System.out.println("YouTube-DL: " + response);
						if (response.startsWith("https://")) {

							String id = UUID.randomUUID().toString().split("-")[0];
							try {
								phase.editMessage(user.getAsMention() + " -> ダウンロードしています...").complete();
								System.out.println("Downloading file...");
								File m4a = new File(file, id + ".m4a");
								FileUtils.copyURLToFile(new URL(response), new File(file, id + ".m4a"));
								System.out.println("Downloaded.");
								phase.editMessage(user.getAsMention() + " -> ダウンロード完了。変換中...").complete();

								instance.runCommand(new String[]{ "ffmpeg", "-i", m4a.getAbsolutePath(), mp3.getAbsolutePath() }, new Callback() {
									@Override
									public void response(String response) {
										System.out.println(response);
									}

									@Override
									public void processEnded() {
										new Thread(() -> {
											try {
												Thread.sleep(1000);
											} catch (InterruptedException e) {
											}

											phase.editMessage(user.getAsMention() + " -> 変換完了。送信準備を行っています...").complete();
											System.out.println(mp3.exists());
											if (mp3.exists()) {
												EmbedBuilder embedBuilder = new EmbedBuilder();
												embedBuilder.setColor(Color.RED);
												embedBuilder.addField("タイトル", instance.getConfig().getString("YouTube." + videoId + ".Title"), true);
												embedBuilder.addField("長さ", instance.getConfig().getString("YouTube." + videoId + ".Duration"), true);
												embedBuilder.setDescription("ファイルが準備できました。\nファイルは48時間後削除されます。");
												Message message = channel.sendFile(mp3).embed(embedBuilder.build()).complete();
												phase.delete().complete();

												try {
													Thread.sleep(1000 * 60 * 60 * 48);
												} catch (Exception e) {
												}

												message.delete().complete();
											}
										}).start();
									}
								});
							} catch (Exception e) {
								channel.sendMessage("エラー発生よ。").complete();
								e.printStackTrace();
							}
						} else {
							channel.sendMessage("エラー発生よ。").complete();
						}
					}

					@Override
					public void processEnded() {
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
