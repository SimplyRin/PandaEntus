package net.simplyrin.pandaentus.commands.general;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.Version;

/**
 * Created by SimplyRin on 2020/07/13.
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
public class StatsCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!stats";
	}
	
	@Override
	public List<String> getAlias() {
		return null;
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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
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
