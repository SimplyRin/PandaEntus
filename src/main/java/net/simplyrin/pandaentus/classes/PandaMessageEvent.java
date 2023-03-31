package net.simplyrin.pandaentus.classes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

/**
 * Created by SimplyRin on 2021/11/16.
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
@Getter
@AllArgsConstructor
public class PandaMessageEvent {
	
	private User user;
	private MessageChannel channel;
	private ChannelType channelType;
	private Guild guild;
	private JDA jda;
	private Message message;
	private Member member;

	private SlashCommandInteractionEvent slashCommandEvent;
	private ReplyCallbackAction replyAction;

	public User getAuthor() {
		return this.user;
	}
	
	public Message reply(String message) {
		if (this.isSlashCommand()) {
			return this.replyAction.setContent(message).complete().retrieveOriginal().complete();
		} else {
			this.channel.sendTyping().complete();
			return this.channel.sendMessage(message).complete();
		}
	}
	
	public Message reply(EmbedBuilder embedBuilder) {
		return this.reply(embedBuilder.build());
	}
	
	public Message reply(MessageEmbed embed) {
		if (this.isSlashCommand()) {
			return this.replyAction.addEmbeds(embed).complete().retrieveOriginal().complete();
		} else {
			this.channel.sendTyping().complete();
			return this.channel.sendMessageEmbeds(embed).complete();
		}
	}
	
	public boolean isSlashCommand() {
		return this.slashCommandEvent != null;
	}
	
	public static PandaMessageEvent get(MessageReceivedEvent event) {
		return new PandaMessageEvent(event.getAuthor(), event.getChannel(), event.getChannelType(), event.getGuild(), event.getJDA(), 
				event.getMessage(), event.getMember(), null, null);
	}
	
	public static PandaMessageEvent get(SlashCommandInteractionEvent event) {
		return new PandaMessageEvent(event.getUser(), event.getChannel(), event.getChannelType(), event.getGuild(), event.getJDA(), 
				null, event.getMember(), event, event.deferReply());
	}

}
