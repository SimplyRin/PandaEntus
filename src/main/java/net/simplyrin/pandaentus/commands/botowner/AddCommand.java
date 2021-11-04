package net.simplyrin.pandaentus.commands.botowner;

import java.util.List;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

/**
 * Created by SimplyRin on 2020/10/20.
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
public class AddCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!add";
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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		if (args.length > 3) {
			Configuration config = instance.getConfig();

			config.set("LocalMessage." + channel.getId() + ".Message", args[1]);
			config.set("LocalMessage." + channel.getId() + ".Value.1", args[2]);
			config.set("LocalMessage." + channel.getId() + ".Value.2", args[3]);

			channel.sendMessage("設定しました。").complete();
			return;
		}

		channel.sendMessage("使用方法: !add <メッセージ> <内容1> <内容2>").complete();
		return;
	}

}
