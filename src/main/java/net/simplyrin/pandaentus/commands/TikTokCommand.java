package net.simplyrin.pandaentus.commands;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;
import net.simplyrin.pandaentus.utils.ThreadPool;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;

/**
 * Created by SimplyRin on 2020/11/23.
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
public class TikTokCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "https://vt.tiktok.com/";
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
		File file = new File(new File("tiktok"), "api.php");
		ProcessManager.runCommand(new String[] { "/usr/bin/php", file.getAbsolutePath(), event.getMessage().getContentRaw() }, new Callback() {
			@Override
			public void line(String response) {
				System.out.println("TikTokCommand.java: " + response);

				MessageChannel channel = event.getChannel();

				boolean mention = false;
				if (event.getGuild().getTextChannelsByName("tiktok", true) != null) {
					mention = true;
					channel = (MessageChannel) event.getGuild().getTextChannelsByName("tiktok", true).get(0);
				}

				System.out.println("TikTokCommand.java: TikTok URL sending to #" + channel.getName());

				JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
				Message message = channel.sendMessage((mention ? event.getAuthor().getAsMention() + " " : "") + jsonObject.get("url").getAsString()).complete();

				String path = jsonObject.get("path").getAsString();
				File file = new File(path);
				file.deleteOnExit();

				ThreadPool.run(() -> {
					try {
						TimeUnit.DAYS.sleep(1);
					} catch (Exception e) {
					}

					System.out.println("Delete: " + path);
					file.delete();
					message.delete().complete();
				});
			}
		}, true);
	}

}
