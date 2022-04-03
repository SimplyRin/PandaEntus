package net.simplyrin.pandaentus.commands.serveradmin;

import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2022/02/09.
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
public class ChannelManagementCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!channelmanagement";
	}

	@Override
	public String getDescription() {
		return null;
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
		return CommandPermission.ServerAdministrator;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		
		Configuration config = instance.getConfig();
		
		String path = "Server." + guild.getId() + ".DisableChannelManagement";
		
		boolean bool = config.getBoolean(path, false);
		bool = !bool;

		config.set(path, bool);
		
		channel.sendMessage("通話チャンネル管理機能を **" + (bool ? "無効" : "有効") + "** にしました。").complete();
	}

}