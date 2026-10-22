package net.simplyrin.pandaentus.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
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
public class CommandExecutor extends ListenerAdapter {

	private PandaEntus instance;

	private HashMap<String, BaseCommand> map = new HashMap<>();

	public CommandExecutor(PandaEntus instance) {
		this.instance = instance;
	}

	public void registerCommand(CommandListUpdateAction commands, String command, BaseCommand baseCommand) {
		if (command == null) {
			throw new NullPointerException(baseCommand.getClass().getName() + "#getCommand() is null!");
		}
		this.map.put(command, baseCommand);

		System.out.println("[Command:Register] " + command);
		if (baseCommand.getCommandData() != null) {
			System.out.println("[Command:Register|Slash] " + command);
			commands.addCommands(baseCommand.getCommandData());
		}

		if (baseCommand.getAliases() != null && !baseCommand.getAliases().isEmpty()) {
			for (String alias : baseCommand.getAliases()) {
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
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		Guild guild = event.getGuild();
		if (guild == null) {
			return;
		}

		Member member = event.getMember();
		User user = event.getUser();
		if (user.isBot()) {
			return;
		}

		String raw = this.getCommandPath(event);
		String[] args = raw.split("[\\/]");

		args[0] = "!" + args[0];

		System.out.println(
				guild.getName() + "@" + guild.getIdLong() + ", " + user.getName() + ": " + raw + " (" + args[0] + ")");

		if (args.length > 0) {
			this.check(args, user, member, PandaMessageEvent.get(event), raw);
		}
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
			this.check(args, user, member, PandaMessageEvent.get(event), raw);
		}
	}

	public void check(String[] args, User user, Member member, PandaMessageEvent event, String raw) {
		for (BaseCommand baseCommand : this.map.values()) {
			List<String> commands = new ArrayList<>();

			commands.add(baseCommand.getCommand());
			if (baseCommand.getAliases() != null && baseCommand.getAliases().size() >= 1) {
				commands.addAll(baseCommand.getAliases());
			}

			// 名前やエイリアスが複数一致しても、1 つのコマンドは 1 回だけ実行する
			boolean matched = false;
			for (String command : commands) {
				switch (baseCommand.getType()) {
				case EqualsIgnoreCase:
					matched = args[0].equalsIgnoreCase(command);
					break;
				case StartsWith:
					matched = args[0].toLowerCase().startsWith(command.toLowerCase());
					break;
				case Contains:
					matched = args[0].toLowerCase().contains(command.toLowerCase());
					break;
				default:
					break;
				}
				if (matched) {
					break;
				}
			}

			if (matched) {
				this.execute(baseCommand, user, member, event, raw, args);
			}
		}
	}

	public void execute(BaseCommand baseCommand, User user, Member member, PandaMessageEvent event, String raw,
			String[] args) {
		if (baseCommand.getPermission().equals(CommandPermission.BotOwner) && !this.instance.isBotOwner(user)) {
			return;
		}

		if (baseCommand.getPermission().equals(CommandPermission.ServerAdministrator)
				&& !member.hasPermission(Permission.MANAGE_SERVER)) {
			return;
		}

		System.out.println(user.getName() + " (" + user.getId() + "), Executed: " + raw);

		if (baseCommand.sendTyping()) {
			event.getChannel().sendTyping().complete();
		}

		baseCommand.execute(this.instance, event, args);
	}

	public String getCommandPath(SlashCommandInteractionEvent event) {
		StringBuilder builder = new StringBuilder(event.getName());
		if (event.getSubcommandGroup() != null) {
			builder.append('/').append(event.getSubcommandGroup());
		}
		if (event.getSubcommandName() != null) {
			builder.append('/').append(event.getSubcommandName());
		}
		return builder.toString();
	}

}
