package net.simplyrin.pandaentus.commands.botowner;

import java.util.List;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2021/11/21.
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
public class RestartCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!restart";
	}

	@Override
	public String getDescription() {
		return "Bot を再起動します。";
	}

	@Override
	public CommandData getCommandData() {
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
		return CommandPermission.BotOwner;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("now")) {
				event.reply("再起動しています...。");
				
				final String brs = instance.getConfig().getString("Bot.Restart-Script");
				
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
							
							System.out.println("Restarting... isWindows: " + isWindows);
							
							Runtime.getRuntime().exec((isWindows ? "cmd /c start " : "sh ") + brs);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				System.exit(0);
				return;
			}
			
			if (args[1].equalsIgnoreCase("inactive")) {
				event.reply("この機能はまだ実装されていません。");
				return;
			}
		}
		
		event.reply("使用方法: /restart <now|inactive>\n"
				+ "- now: Bot を今すぐ再起動します。\n"
				+ "- inactive: 通話が進行している場合、全ての通話が終了した時 Bot を再起動します。");
	}

}
