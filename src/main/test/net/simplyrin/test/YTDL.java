package net.simplyrin.test;

import java.io.File;
import java.util.UUID;

import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.Main.Callback;

/**
 * Created by SimplyRin on 2020/06/27.
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
public class YTDL {

	public static void main(String[] args) {
		Main instance = new Main();
		instance.runCommand(new String[] { "youtube-dl", "https://music.youtube.com/watch?v=r7TfPL1npOY" }, new Callback() {
			File file;
			@Override
			public void response(String response) {
				if (response.startsWith("[ffmpeg] Merging formats into")) {
					String title = response.replace("[ffmpeg] Merging formats into", "").replace("\"", "").trim();
					System.out.println("Title: " + title);
					this.file = new File(title + ".mkv");
				}
				if (response.startsWith("[download]") && response.contains(".mkv")) {
					String title = response.replace("[download]", "").split(".mkv")[0].trim() + ".mkv";
					System.out.println("Title: " + title);
					this.file = new File(title);
				}
			}

			@Override
			public void processEnded() {
				File mp3 = new File("output_" + UUID.randomUUID().toString().split("-")[0] + ".mp3");

				System.out.println(this.file.exists());

				instance.runCommand(new String[] { "ffmpeg", "-i", this.file.getAbsolutePath(), mp3.getAbsolutePath() }, new Callback() {
					@Override
					public void response(String response) {
						System.out.println(response);
					}

					@Override
					public void processEnded() {
					}
				});
			}
		});
	}

}
