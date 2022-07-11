package net.simplyrin.pandaentus.commands.minigame;

import java.io.File;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.config.Config;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
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
public class WordWolfCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!wordwolf";
	}

	@Override
	public String getDescription() {
		return "ワードウルフゲームに接続";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("wordwolf", this.getDescription())
				.addOption(OptionType.INTEGER, "人狼の人数", "人狼の人数を入力", true)
				.addOption(OptionType.STRING, "時間", "話し合いの時間を指定 (分:秒, HH:ss)", true)
				.addOption(OptionType.STRING, "テーマ", "選ばれるお題のテーマを指定 (入力しなければランダム)");
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
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[s.getOptions().size() + 1];
			args[0] = this.getCommand();
			args[1] = s.getOption("人狼の人数").getAsString();
			args[2] = s.getOption("時間").getAsString();
			args[3] = s.getOption("テーマ") != null ? s.getOption("テーマ").getAsString() : null;
		}
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("start")) {
				WordWolfManager wwm = WordWolfManager.getGameByChannel(channel);
				if (wwm != null) {
					if (wwm.start() == null) {
						event.reply("ゲームは既に開始されています。");
					}
					return;
				}

				event.reply("このチャンネルで開始を予定しているゲームを見つけることができませんでした。\n" + this.getCommand() + " コマンドを使用してゲームの開始を予約してください。");
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
				event.reply("値は数字で入力してください。");
				return;
			}
			
			if (WordWolfManager.getGameByChannel(channel) != null) {
				event.reply("既に開始されているゲームがあります。");
				return;
			}
			
			WordWolfManager wwm = new WordWolfManager(instance, event.getGuild(), channel, wolfs, time, theme);
			wwm.startRecruit(event);
			return;
		}
		
		String theme = "";
		
		File folder = new File("wordwolf");
		folder.mkdirs();
		
		for (File file : folder.listFiles()) {
			Configuration config = Config.getConfig(file);
			theme +=  config.getString("name") + "\n";
		}
		
		String reply = args[0] + " <ワードウルフの人数> <時間 (分:秒)> <トークテーマ, 入力しなければランダム>\n"
				+ "開始するには `" + args[0] + " start` と入力してください。\n"
				+ "```テーマ:\n"
				+ theme + "```";
		event.reply(reply);
	}

}
