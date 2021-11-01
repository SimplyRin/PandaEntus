package net.simplyrin.pandaentus.listeners;

import java.util.HashMap;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;

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
public class CommandExecutor extends ListenerAdapter {

	private PandaEntus instance;
	private HashMap<String, BaseCommand> map = new HashMap<>();

	public CommandExecutor(PandaEntus instance) {
		this.instance = instance;
	}

	public void registerCommand(String command, BaseCommand baseCommand) {
		if (command == null) {
			throw new NullPointerException(baseCommand.getClass().getName() + "#getCommand() is null!");
		}
		this.map.put(command, baseCommand);
		System.out.println("[Command:Register] " + command);
	}

	public void unregisterCommand(String command) {
		this.map.remove(command);
		System.out.println("[Command:Un-Register] " + command);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return;
		}
		Member member = event.getMember();
		User user = event.getAuthor();
		if (user.isBot()) {
			return;
		}

		String raw = event.getMessage().getContentRaw();
		String[] args = raw.split(" ");

		if (args.length > 0) {
			for (BaseCommand baseCommand : this.map.values()) {
				switch (baseCommand.getType()) {
				case EqualsIgnoreCase:
					if (args[0].equalsIgnoreCase(baseCommand.getCommand())) {
						if (baseCommand.getPermission().equals(CommandPermission.BotOwner) && !this.instance.isBotOwner(user)) {
							return;
						}
						
						if (baseCommand.getPermission().equals(CommandPermission.ServerAdministrator) && !member.hasPermission(net.dv8tion.jda.api.Permission.MANAGE_SERVER)) {
							return;
						}

						System.out.println(user.getName() + " (" + user.getId() + "), Executed: " + raw);
						baseCommand.execute(this.instance, event, args);
					}
					break;
				case StartsWith:
					if (args[0].toLowerCase().startsWith(baseCommand.getCommand().toLowerCase())) {
						if (baseCommand.getPermission().equals(CommandPermission.BotOwner) && !this.instance.isBotOwner(user)) {
							return;
						}
						
						if (baseCommand.getPermission().equals(CommandPermission.ServerAdministrator) && !member.hasPermission(net.dv8tion.jda.api.Permission.MANAGE_SERVER)) {
							return;
						}

						System.out.println(user.getName() + " (" + user.getId() + "), Executed: " + raw);
						baseCommand.execute(this.instance, event, args);
					}
					break;
				default:
					break;
				}
			}
		}
	}

}
