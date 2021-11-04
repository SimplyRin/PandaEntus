package net.simplyrin.pandaentus.commands.general;

import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.utils.AkinatorManager;
import net.simplyrin.pandaentus.utils.ThreadPool;

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
public class AkinatorCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!akinator";
	}
	
	@Override
	public String getDescription() {
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
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		channel.sendMessage("**アキネーターに接続しています...。\n\n中止する場合、\"やめる\" と発言してください。\n一つ戻る場合、\"もどる\" と発言してください。**").complete();
		channel.sendTyping().complete();
		ThreadPool.run(() -> {
			instance.getAkiMap().put(channel.getId(), new AkinatorManager(event.getGuild(), channel.getId()));
			AkinatorManager am = instance.getAkiMap().get(channel.getId());

			EmbedBuilder embedBuilder = new EmbedBuilder();
			am.setEmbed(embedBuilder);
			String latestId = channel.sendMessage(embedBuilder.build()).complete().getId();
			am.setLatestMessageId(latestId);
		});
	}

}
