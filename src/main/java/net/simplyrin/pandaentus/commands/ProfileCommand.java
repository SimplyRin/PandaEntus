package net.simplyrin.pandaentus.commands;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class ProfileCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!profile";
	}
	
	@Override
	public String getDescription() {
		return "あなたのアカウント作成日を表示";
	}
	
	@Override
	public CommandData getCommandData() {
		return new CommandData("profile", this.getDescription())
				.addOption(OptionType.USER, "ユーザー", "指定したユーザーのアカウント作成日を確認", false);
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
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setColor(Color.ORANGE);
		
		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[2];
			args[0] = this.getCommand();
			args[1] = s.getOption("ユーザー") != null ? s.getOption("ユーザー").getAsString() : event.getUser().getId();
		}

		Member member = event.getMember();
		if (args.length > 1) {
			try {
				member = event.getGuild().getMemberById(args[1].replace("<", "").replace(">", "").replace("@", "").replace("!", ""));
			} catch (Exception e) {
			}
		}

		Date date = new Date(member.getUser().getTimeCreated().toInstant().toEpochMilli());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		embedBuilder.addField("アカウント名", instance.getNickname(member), false);
		embedBuilder.addField("アカウント作成日", simpleDateFormat.format(date), false);
		
		event.reply(embedBuilder.build());
		return;
	}

}
