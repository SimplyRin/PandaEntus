package net.simplyrin.pandaentus.commands;

import java.util.List;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2020/07/17.
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
public class IconCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!icon";
	}
	
	@Override
	public String getDescription() {
		return "アカウントのアイコンを表示";
	}
	
	@Override
	public boolean isAllowedToRegisterSlashCommand() {
		return true;
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
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		if (args.length > 1) {
			String id = args[1];
			id = id.replace("@", "");
			id = id.replace("<", "");
			id = id.replace(">", "");
			id = id.replace("!", "");

			Member member = event.getGuild().getMemberById(id);
			if (member != null) {
				channel.sendMessage(member.getUser().getAvatarUrl()).complete();
			} else {
				channel.sendMessage("ユーザーが見つかりませんでした。").complete();
			}
			return;
		}

		event.reply("使用方法: !icon <ユーザーID|メンション>");
	}

}
