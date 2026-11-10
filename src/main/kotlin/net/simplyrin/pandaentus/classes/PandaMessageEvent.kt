package net.simplyrin.pandaentus.classes

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction

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
class PandaMessageEvent(
	val user: User,
	val channel: MessageChannel,
	val channelType: ChannelType,
	val guild: Guild,
	val jda: JDA,
	val message: Message?,
	val member: Member,
	val slashCommandEvent: SlashCommandInteractionEvent?,
	val replyAction: ReplyCallbackAction?
) {

	val author: User
		get() = this.user

	fun reply(message: String): Message {
		val replyAction = this.replyAction
		return if (replyAction != null) {
			replyAction.setContent(message).complete().retrieveOriginal().complete()
		} else {
			this.channel.sendTyping().complete()
			this.channel.sendMessage(message).complete()
		}
	}

	fun reply(embedBuilder: EmbedBuilder): Message {
		return this.reply(embedBuilder.build())
	}

	fun reply(embed: MessageEmbed): Message {
		val replyAction = this.replyAction
		return if (replyAction != null) {
			replyAction.addEmbeds(embed).complete().retrieveOriginal().complete()
		} else {
			this.channel.sendTyping().complete()
			this.channel.sendMessageEmbeds(embed).complete()
		}
	}

	fun isSlashCommand(): Boolean {
		return this.slashCommandEvent != null
	}

	companion object {

		/**
		 * サーバー外 (DM など) やメンバーを取得できないイベントの場合は null を返す
		 */
		@JvmStatic
		fun get(event: MessageReceivedEvent): PandaMessageEvent? {
			if (!event.isFromGuild) {
				return null
			}
			val member = event.member ?: return null

			return PandaMessageEvent(event.author, event.channel, event.channelType, event.guild, event.jda,
				event.message, member, null, null)
		}

		/**
		 * サーバー外 (DM など) やメンバーを取得できないイベントの場合は null を返す
		 */
		@JvmStatic
		fun get(event: SlashCommandInteractionEvent): PandaMessageEvent? {
			val guild = event.guild ?: return null
			val member = event.member ?: return null

			return PandaMessageEvent(event.user, event.channel, event.channelType, guild, event.jda,
				null, member, event, event.deferReply())
		}

	}

}
