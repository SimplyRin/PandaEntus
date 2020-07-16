package net.simplyrin.pandaentus.listeners;

import java.util.HashMap;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.Permission;

/**
 * Created by SimplyRin on 2020/07/09.
 *
 * Copyright (c) 2020 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class CommandExecutor extends ListenerAdapter {

	private Main instance;
	private HashMap<String, BaseCommand> map = new HashMap<>();

	public CommandExecutor(Main instance) {
		this.instance = instance;
	}

	public void registerCommand(String command, BaseCommand baseCommand) {
		if (command == null) {
			throw new RuntimeException(baseCommand.getClass().getName() + "#getCommand() is null!");
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
		User user = event.getAuthor();
		if (user.isFake() || user.isBot()) {
			return;
		}

		String raw = event.getMessage().getContentRaw();
		String[] args = raw.split(" ");

		if (args.length > 0) {
			for (BaseCommand baseCommand : this.map.values()) {
				switch (baseCommand.getType()) {
				case EqualsIgnoreCase:
					if (args[0].equalsIgnoreCase(baseCommand.getCommand())) {
						if (baseCommand.getPermission().equals(Permission.Administrator) && !user.getId().equals(this.instance.getAdminId())) {
							return;
						}

						baseCommand.execute(this.instance, event, args);
					}
					break;
				case StartsWith:
					if (args[0].toLowerCase().startsWith(baseCommand.getCommand().toLowerCase())) {
						if (baseCommand.getPermission().equals(Permission.Administrator) && !user.getId().equals(this.instance.getAdminId())) {
							return;
						}

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
