package net.simplyrin.pandaentus.commands.minigame;

import java.io.File;
import java.util.List;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.config.Config;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.gamemanager.wordwolf.WordWolfManager;

/**
 * Created by SimplyRin on 2021/11/09.
 *
 * Copyright (C) 2021 SimplyRin
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
public class WordWolfCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!wordwolf";
	}

	@Override
	public String getDescription() {
		return null;
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
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("start")) {
				WordWolfManager wwm = WordWolfManager.getGameByChannel(channel);
				if (wwm != null) {
					if (wwm.start() == null) {
						channel.sendMessage("ゲームは既に開始されています。").complete();
					}
					return;
				}
				channel.sendMessage("このチャンネルで開始を予定しているゲームを見つけることができませんでした。\n" + this.getCommand() + " コマンドを使用してゲームの開始を予約してください。").complete();
				return;
			}
		}
		
		if (args.length > 2) {
			String theme = null;
			
			if (args.length > 3) {
				theme = args[3];
			}
			
			int wolfs;
			int time;
			try {
				wolfs = Integer.valueOf(args[1]);
				time = instance.mmssToSeconds(args[2]);
			} catch (Exception e) {
				channel.sendMessage("値は数字で入力してください。").complete();
				return;
			}
			
			if (WordWolfManager.getGameByChannel(channel) != null) {
				channel.sendMessage("既に開始されているゲームがあります。").complete();
				return;
			}
			
			WordWolfManager wwm = new WordWolfManager(instance, event.getGuild(), channel, wolfs, time, theme);
			wwm.startRecruit();
			return;
		}
		
		String theme = "";
		
		File folder = new File("wordwolf");
		folder.mkdirs();
		
		for (File file : folder.listFiles()) {
			Configuration config = Config.getConfig(file);
			theme +=  config.getString("name") + "\n";
		}
		channel.sendMessage(args[0] + " <ワードウルフの人数> <時間 (分:秒)> <トークテーマ, 入力しなければランダム>\n"
				+ "開始するには `" + args[0] + " start` と入力してください。\n"
				+ "```テーマ:\n"
				+ theme + "```").complete();
	}

}
