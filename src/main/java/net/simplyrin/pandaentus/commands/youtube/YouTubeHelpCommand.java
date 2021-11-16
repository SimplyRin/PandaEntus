package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import com.google.common.reflect.ClassPath;

import net.dv8tion.jda.api.EmbedBuilder;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2021/11/04.
 *
 * Copyright (C) 2021 SimplyRin
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
public class YouTubeHelpCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!ythelp";
	}
	
	@Override
	public String getDescription() {
		return "Music Bot のヘルプを表示";
	}
	
	@Override
	public boolean isAllowedToRegisterSlashCommand() {
		return true;
	}

	@Override
	public List<String> getAlias() {
		return Arrays.asList("!youtube");
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
		event.reply(this.getHelpEmbed(instance).build());
	}
	
	public String getHelpMessage() {
		return "🎵 Music Bot 機能コマンド一覧 / ヘルプ";
	}
	
	public EmbedBuilder getHelpEmbed(PandaEntus instance) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		
		embedBuilder.setAuthor(this.getHelpMessage());
		embedBuilder.setColor(Color.CYAN);
		
		try {
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			for (final ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClasses()) {
				if (classInfo.getName().startsWith(YouTubeHelpCommand.class.getPackageName())) {
					BaseCommand command = instance.getCommandRegister().getRegisteredCommand(Class.forName(classInfo.getName()));

					if (command.getDescription() != null) {
						String aliases = null;
						if (command.getAlias() != null) {
							for (String alias : command.getAlias()) {
								if (aliases == null) {
									aliases = "";
								}
								aliases += alias + ", ";
							}
						}
						if (aliases != null) {
							aliases = aliases.substring(0, aliases.length() - 2);
						}
						embedBuilder.addField(command.getCommand() + (aliases != null ? " (" + aliases + ")" : ""), command.getDescription(), true);
					}
				}
			}
		} catch (Exception e) {
			instance.postError(e);
			return null;
		}
		return embedBuilder;
	}

}
