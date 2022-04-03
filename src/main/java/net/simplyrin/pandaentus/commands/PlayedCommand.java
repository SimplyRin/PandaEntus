package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2022/04/03.
 *
 * Copyright (C) 2022 SimplyRin
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
public class PlayedCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!played";
	}

	@Override
	public String getDescription() {
		return "プレイしているゲームのプレイ時間記録を管理";
	}

	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("played", this.getDescription());
	}

	@Override
	public List<String> getAlias() {
		return Arrays.asList("t!played");
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
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		var guild = event.getGuild();
		var member = event.getMember();
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("enable")) {
				instance.getConfig().set("Guild." + guild.getId() + "." + member.getId() + ".IsEnabledActivity", true);
				
				event.reply("ゲームプレイ記録を **有効** にしました。");
				return;
			}
			
			if (args[1].equalsIgnoreCase("disable")) {
				instance.getConfig().set("Guild." + guild.getId() + "." + member.getId() + ".IsEnabledActivity", false);
				
				event.reply("ゲームプレイ記録を **無効** にしました。");
				return;
			}
		}
		
		var enabled = instance.getConfig().getBoolean("Guild." + guild.getId() + "." + member.getId() + ".IsEnabledActivity", false);
		
		// 記録表示
		if (enabled) {
			HashMap<String, Integer> games = new HashMap<>();
			
			var baseKey = "Guild." + guild.getId() + "." + member.getId() + ".Game";
			for (String gameName : instance.getActivityConfig().getSection(baseKey).getKeys()) {
				int played = instance.getActivityConfig().getInt(baseKey + "." + gameName);
				
				games.put(gameName, played);
			}
			
			List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>(games.entrySet());
			Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return ((Integer) o2.getValue()).compareTo((Integer) o1.getValue());
				}
			});
			
			for (int i = 0; i < 5; i++) {
				if (entries.size() > i) {
					var game = entries.get(i).getKey();
					var played = entries.get(i).getValue();
					
					var calendar = Calendar.getInstance();
					calendar.add(Calendar.MINUTE, -played);
					
					int hours = played / 60; // since both are ints, you get an int
					int minutes = played % 60;
					
					String hhmm = "";
					if (hours >= 1) {
						hhmm = hours + "時間" + minutes + "分";
					} else {
						hhmm = minutes + "分";
					}

					embedBuilder.addField(game, instance.getUptime(calendar.getTime()) + " (" + hours + "時間)", false);
				}
			}
			
			embedBuilder.setAuthor("の ゲーム記録", null, member.getUser().getAvatarUrl());
			embedBuilder.setColor(Color.WHITE);
			embedBuilder.setFooter("'" + args[0] + " disable' で記録をオフにできます。");
			event.reply(embedBuilder);
			
			return;
		}
		
		embedBuilder.setColor(Color.RED);
		embedBuilder.setDescription("あなたのゲームプレイ記録は現在無効に設定されています。有効にするには、`" + args[0] + " enable` と入力してください。");
		
		event.reply(embedBuilder);
	}

}
