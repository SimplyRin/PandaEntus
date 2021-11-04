package net.simplyrin.pandaentus.commands.serveradmin;

import java.util.List;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

/**
 * Created by SimplyRin on 2020/07/09.
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
public class InitCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!init";
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
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
			Category textCategory = guild.createCategory("Text Channels").complete();
			TextChannel textChannel = textCategory.createTextChannel("general-1").complete();
			Category voiceCategory = guild.createCategory("Voice Channels").complete();
			voiceCategory.createVoiceChannel("General-1").setUserlimit(99).complete();
			textChannel.sendMessage("Bot で必要なカテゴリ、チャンネルを自動作成しました。").complete();
			return;
		}

		channel.sendMessage("!init confirm と入力すると、Bot で必要なカテゴリ、チャンネルを自動的に作成します。\n"
				+ "作成されるチャンネルは以下の通りです。\nカテゴリ: `Text Channels`, `Voice Channels`"
				+ "\nテキストチャンネル: `#general`"
				+ "\nボイスチャンネル: `General-1`").complete();
	}

}
