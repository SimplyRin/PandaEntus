package net.simplyrin.pandaentus.commands;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;
import net.simplyrin.pandaentus.utils.Version;

/**
 * Created by SimplyRin on 2020/07/13.
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
public class StatsCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!stats";
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

		Date date = instance.getStartupDate();
		Runtime runtime = Runtime.getRuntime();
		JDA jda = instance.getJda();

		String startup = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);

		String message = "```";
		message += "起動日: " + startup + "\n";
		message += "稼働日: " + instance.getUptime(date) + "\n\n";

		message += "利用可能コア数: " + runtime.availableProcessors() + "\n\n";

		message += "メモリ空き状態:\n";
		message += "  空き状態: " + instance.formatSize(runtime.freeMemory()) + "\n";
		message += "  使用容量: " + instance.formatSize(runtime.totalMemory()) + "\n";
		message += "  最大容量: " + (runtime.maxMemory() == Long.MAX_VALUE ? "無制限" : instance.formatSize(runtime.maxMemory())) + "\n\n";

		message += "サーバー情報:\n";
		message += "  サーバー数: " + jda.getGuilds().size() + "\n";
		message += "  テキストチャンネル数: " + jda.getTextChannels().size() + "\n";
		message += "  ボイスチャンネル数: " + jda.getVoiceChannels().size() + "\n";
		message += "  ユーザー数: " + jda.getUsers().size() + "\n\n";

		message += "バージョン情報:\n";
		message += "  Java: " + System.getProperty("java.version") + "\n";
		message += "  PandaEntus: " + Version.POMVERSION + " (" + Version.BUILD_TIME + ")\n";
		message += "  JDA: " + JDAInfo.VERSION + "\n";
		message += "  LavaPlayer: " + PlayerLibrary.VERSION;

		channel.sendMessage(message + "```").complete();
	}

}
