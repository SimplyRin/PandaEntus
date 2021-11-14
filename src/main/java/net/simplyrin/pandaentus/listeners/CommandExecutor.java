package net.simplyrin.pandaentus.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.Permission;
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
		if (baseCommand.getAlias() != null && !baseCommand.getAlias().isEmpty()) {
			for (String alias : baseCommand.getAlias()) {
				System.out.println("[Command:Register-Alias] - " + alias);
			}
		}
	}

	public void unregisterCommand(String command) {
		this.map.remove(command);
		System.out.println("[Command:Un-Register] " + command);
	}
	
	public BaseCommand getCommand(String command) {
		return this.map.get(command);
	}
	
	public BaseCommand getRegisteredCommand(Class<?> clazz) {
		BaseCommand command = null;
		for (BaseCommand baseCommand : this.map.values()) {
			if (baseCommand.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
				command = baseCommand;
			}
		}
		return command;
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
				List<String> commands = new ArrayList<>();
				
				commands.add(baseCommand.getCommand());
				if (baseCommand.getAlias() != null && baseCommand.getAlias().size() >= 1) {
					commands.addAll(baseCommand.getAlias());
				}
				
				switch (baseCommand.getType()) {
				case EqualsIgnoreCase:
					for (String command : commands) {
						if (args[0].equalsIgnoreCase(command)) {
							this.execute(baseCommand, user, member, event, raw, args);
						}
					}
					break;
				case StartsWith:
					for (String command : commands) {
						if (args[0].toLowerCase().startsWith(command.toLowerCase())) {
							this.execute(baseCommand, user, member, event, raw, args);
						}
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	public void execute(BaseCommand baseCommand, User user, Member member, MessageReceivedEvent event, String raw, String[] args) {
		if (baseCommand.getPermission().equals(CommandPermission.BotOwner) && !this.instance.isBotOwner(user)) {
			return;
		}
		
		if (baseCommand.getPermission().equals(CommandPermission.ServerAdministrator) && !member.hasPermission(Permission.MANAGE_SERVER)) {
			return;
		}

		System.out.println(user.getName() + " (" + user.getId() + "), Executed: " + raw);
		baseCommand.execute(this.instance, event, args);
	}

}
