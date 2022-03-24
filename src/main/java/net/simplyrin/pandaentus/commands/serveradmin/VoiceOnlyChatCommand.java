package net.simplyrin.pandaentus.commands.serveradmin;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2022/03/23.
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
public class VoiceOnlyChatCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!voiceonlychat";
	}

	@Override
	public String getDescription() {
		return "通話に参加しているユーザーのみが送信することのできる専用のテキストチャンネルを作成します。";
	}

	@Override
	public CommandData getCommandData() {
		return null;
	}

	@Override
	public List<String> getAlias() {
		return Arrays.asList("!voc");
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
		var guild = event.getGuild();
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("create")) {
				if (args.length > 2) {
					var channel = guild.createTextChannel(args[2]).complete();
					
					instance.getConfig().set("VoiceOnlyChat." + guild.getId() + ".ID", channel.getId());

					event.reply("通話参加者専用テキストチャンネルを作成しました。( " + channel.getAsMention() + " )\n"
							+ "デフォルトではユーザーがチャンネルへのメッセージを送信できないよう権限を設定してください。");
					return;
				}
				
				event.reply("使用方法: " + this.getCommand() + " create <チャンネル名>");
				return;
			}
			
			if (args[1].equalsIgnoreCase("delete")) {
				var channel = guild.getTextChannelById(instance.getConfig().getString("VoiceOnlyChat." + guild.getId() + ".ID"));
				if (channel == null) {
					event.reply("このサーバーでは、通話参加者専用テキストチャンネルがまだ作成されていません。\n" 
							+ this.getCommand() + " を使用してチャンネルを作成してください。");
					return;
				}
				
				channel.delete().complete();
				
				instance.getConfig().set("VoiceOnlyChat." + guild.getId() + ".Role", null);
				instance.getConfig().set("VoiceOnlyChat." + guild.getId() + ".ID", null);
				event.reply("通話参加者専用テキストチャンネルを削除しました。");
				return;
			}
		}
		
		event.reply("使用方法: " + this.getCommand() + " <create|delete> [チャンネル名]");
		return;
	}
	
	public void join(PandaEntus instance, Member member) {
		var guild = member.getGuild();
		var id = instance.getConfig().getString("VoiceOnlyChat." + guild.getId() + ".ID", null);
		if (id == null) {
			return;
		}
		
		var channel = guild.getTextChannelById(id);
		
		if (channel == null) {
			return;
		}
		
		var override = channel.getPermissionOverride(member);
		
		if (override != null) {
			override.getManager().setAllow(Permission.MESSAGE_WRITE).complete();
		} else {
			channel.createPermissionOverride(member).setAllow(Permission.MESSAGE_WRITE).complete();
		}
	}
	
	public void quit(PandaEntus instance, Member member) {
		var guild = member.getGuild();
		var id = instance.getConfig().getString("VoiceOnlyChat." + guild.getId() + ".ID", null);
		if (id == null) {
			return;
		}
		
		var channel = guild.getTextChannelById(id);
		
		if (channel == null) {
			return;
		}
		
		var override = channel.getPermissionOverride(member);
		
		if (override != null) {
			override.getManager().setDeny(Permission.MESSAGE_WRITE).complete();
		} else {
			channel.createPermissionOverride(member).setDeny(Permission.MESSAGE_WRITE).complete();
		}
	}

}
