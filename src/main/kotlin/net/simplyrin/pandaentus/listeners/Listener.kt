package net.simplyrin.pandaentus.listeners

import java.awt.Color
import java.util.Date

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.Category
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.commands.serveradmin.VoiceOnlyChatCommand

/**
 * Created by SimplyRin on 2019/03/13.
 *
 * Copyright (C) 2019 SimplyRin
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
class Listener(private val instance: PandaEntus) : ListenerAdapter() {

	override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
		val joined = event.channelJoined
		val left = event.channelLeft

		if (joined != null && left != null) {
			this.onPandaGuildVoiceMove(event)
		} else if (joined != null) {
			this.onPandaGuildVoiceJoin(event)
		} else {
			this.onPandaGuildVoiceLeave(event)
		}
	}

	fun onPandaGuildVoiceJoin(event: GuildVoiceUpdateEvent) {
		val member = event.member

		if (member.user.isBot) {
			return
		}

		val guild = event.guild
		val channelJoined = event.channelJoined ?: return

		this.instance.vcNameManager.joinedChannel[guild.id + "-" + member.id] = channelJoined.idLong

		// AcitivityListener からプレイ中のゲームを取得
		val gameList = this.instance.activityListener.map[guild.id + "-" + member.id]
		if (gameList != null && gameList.size >= 1) {
			this.instance.vcNameManager.updateVoiceChannelName(member, gameList)
		}

		// 通話参加者専用チャンネル管理
		this.getVoiceOnlyChatCommand()?.join(this.instance, member)

		val category = this.instance.getVoiceChannelCategory(guild) ?: return

		val voiceChannels = category.voiceChannels
		val parentCategory = guild.getVoiceChannelById(channelJoined.id)?.parentCategory

		println("[" + guild.name + "@" + guild.id + "] #" + channelJoined.name + " JOINED: " + this.getNickname(member) + "@" + member.id)

		if (parentCategory != null) {
			if (category.name != parentCategory.name) {
				return
			}
		}

		this.instance.timeManager.getUser(member.user.id).joined()

		this.check(member, guild)

		if (this.isInVoiceChannels(voiceChannels, member)) {
			this.instance.timeUtils.get(guild.id, member.id).join()
		}

		println("Joined " + channelJoined.name)
	}

	fun onPandaGuildVoiceMove(event: GuildVoiceUpdateEvent) {
		val member = event.member

		if (member.user.isBot) {
			return
		}

		val guild = event.guild
		val channelJoined = event.channelJoined ?: return
		val channelLeft = event.channelLeft ?: return

		this.instance.vcNameManager.updateVoiceChannelName(member, null as List<String>?)

		this.instance.vcNameManager.joinedChannel[guild.id + "-" + member.id] = channelJoined.idLong

		val category = this.instance.getVoiceChannelCategory(guild) ?: return

		val hasMember = this.isInVoiceChannels(category.voiceChannels, member)

		val timeUtils = this.instance.timeUtils.get(guild.id, member.id)

		if (hasMember) {
			println("Join @ " + this.getNickname(member))
			timeUtils.join()
		} else {
			timeUtils.quit()
			println("Quit @ " + this.getNickname(member))

			val leftChannel = guild.getVoiceChannelById(channelLeft.id)
			if (leftChannel != null) {
				this.quitMember(guild, category, null, member, leftChannel, false)
			}
		}

		this.check(member, guild)
	}

	fun check(member: Member, guild: Guild) {
		val category = this.instance.getVoiceChannelCategory(guild) ?: return

		val voiceChannels = category.voiceChannels

		// Bot 以外のメンバーがいるチャンネルの数
		val count = voiceChannels.count { voiceChannel -> voiceChannel.members.any { !it.user.isBot } }

		val path = "Server." + guild.id + ".DisableChannelManagement"

		if (!this.instance.config.getBoolean(path)) {
			if (voiceChannels.size == count) {
				// チャンネルの管理権限がない場合は作成しない
				if (!guild.selfMember.hasPermission(category, Permission.MANAGE_CHANNEL)) {
					println("[" + guild.name + "@" + guild.id + "] Missing permission: MANAGE_CHANNEL (" + category.name + ")")
					return
				}

				val c = count + 1
				category.createVoiceChannel(this.instance.getVoiceChannelName(category) + "-" + c)
					.setUserlimit(99).setBitrate(guild.maxBitrate).complete()
			}
		}
	}

	fun onPandaGuildVoiceLeave(event: GuildVoiceUpdateEvent) {
		val member = event.member

		if (member.user.isBot) {
			return
		}

		val guild = event.guild
		val channelLeft = event.channelLeft ?: return

		// 通話参加者専用チャンネル管理
		this.getVoiceOnlyChatCommand()?.quit(this.instance, member)

		// ちゃんねるめい
		this.instance.vcNameManager.updateVoiceChannelName(member, null as List<String>?)

		this.instance.vcNameManager.joinedChannel.remove(guild.id + "-" + member.id)

		val category = this.instance.getVoiceChannelCategory(guild) ?: return

		this.instance.timeUtils.get(guild.id, member.user.id).quit()

		val leftChannel = guild.getVoiceChannelById(channelLeft.id) ?: return

		this.quitMember(guild, category, leftChannel.parentCategory, member, leftChannel, true)
	}

	fun quitMember(guild: Guild, category: Category, parentCategory: Category?, member: Member, channelLeft: VoiceChannel, leaveEvent: Boolean) {
		println("[" + guild.name + "@" + guild.id + "] #" + channelLeft.name + " LEAVE: " + this.getNickname(member) + "@" + member.id)

		if (leaveEvent) {
			if (parentCategory == null) {
				return
			}

			if (category.name != parentCategory.name) {
				return
			}
		}

		val voiceChannels = category.voiceChannels
		if (voiceChannels.size == 1) {
			return
		}

		// Bot 以外のメンバーがいないチャンネルの数
		val count = voiceChannels.count { voiceChannel -> voiceChannel.members.none { !it.user.isBot } }

		// 全てのチャンネルが空になったら通話ログを送信し、チャンネルを作り直す
		if (voiceChannels.size == count) {
			try {
				this.sendCallLog(guild, category, voiceChannels)
			} catch (e: Exception) {
				this.instance.postError(e)
			}

			val path = "Server." + guild.id + ".DisableChannelManagement"

			if (!this.instance.config.getBoolean(path)) {
				// 途中で削除に失敗してチャンネルが中途半端に残らないよう、先に全チャンネルの管理権限を確認する
				if (!this.canManageChannels(guild, category)) {
					println("[" + guild.name + "@" + guild.id + "] Missing permission: MANAGE_CHANNEL (" + category.name + ")")
					return
				}

				for (voiceChannel in voiceChannels) {
					try {
						voiceChannel.delete().complete()
					} catch (e: Exception) {
						this.instance.postError(e)
						return
					}
				}

				category.createVoiceChannel(this.instance.getVoiceChannelName(category) + "-1")
					.setUserlimit(99).setBitrate(guild.maxBitrate).complete()
			}
		}
	}

	/**
	 * 通話が終了したときの通話ログを送信する
	 */
	private fun sendCallLog(guild: Guild, category: Category, voiceChannels: List<VoiceChannel>) {
		println("Category: " + category.name)
		println("Size: " + voiceChannels.size)

		// General-2 チャンネルの作成日時を通話の開始時刻とする
		val voiceChannel = voiceChannels.getOrNull(1) ?: return
		val time = Date.from(voiceChannel.timeCreated.toInstant())

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)

		if (this.instance.config.getBoolean("Message-Type.Enable-Simple-Mode")) {
			embedBuilder.setAuthor("通話時間: " + this.instance.getUptime(time))

			val callTimes = this.instance.timeUtils.getList(guild.id)
			for (callTime in callTimes) {
				try {
					val tt = callTime.getTime()
					if (tt != "0秒") {
						val targetMember = guild.getMemberById(callTime.name)
						if (targetMember != null) {
							val path = "User." + targetMember.id + "." + guild.id + ".Vanish"
							val isVanish = this.instance.config.getBoolean(path, false)
							if (!isVanish) {
								embedBuilder.setDescription("ユーザーごとの通話時間:")
								embedBuilder.addField(targetMember.effectiveName, callTime.getTime(), true)
							}
						}
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}

			if (callTimes.isNotEmpty()) {
				this.instance.timeUtils.resetGuild(guild.id)
			}
		} else {
			embedBuilder.addField("通話時間", this.instance.getUptime(time), true)
			embedBuilder.addField("開始時刻", this.instance.getNowTime(time), true)
			embedBuilder.addField("終了時刻", this.instance.getNowTime(), true)
		}

		val path = "Server." + guild.id + ".NoSendLogTimeLog"
		if (!this.instance.config.getBoolean(path)) {
			// general チャンネルがあればそこへ、なければテキストチャンネルカテゴリの最初のチャンネルへ送信する
			val textChannel = guild.getTextChannelsByName("general", true).firstOrNull()
				?: this.instance.getTextChannelCategory(guild)?.textChannels?.firstOrNull()

			textChannel?.sendMessageEmbeds(embedBuilder.build())?.complete()
		}
	}

	/**
	 * カテゴリとカテゴリ内の全ボイスチャンネルに対して MANAGE_CHANNEL 権限があるか確認する
	 */
	private fun canManageChannels(guild: Guild, category: Category): Boolean {
		val selfMember = guild.selfMember
		if (!selfMember.hasPermission(category, Permission.MANAGE_CHANNEL)) {
			return false
		}
		return category.voiceChannels.all { selfMember.hasPermission(it, Permission.MANAGE_CHANNEL) }
	}

	fun getNickname(member: Member): String {
		return member.nickname ?: member.user.name
	}

	private fun getVoiceOnlyChatCommand(): VoiceOnlyChatCommand? {
		return this.instance.commandRegister.getRegisteredCommand(VoiceOnlyChatCommand::class.java) as? VoiceOnlyChatCommand
	}

	private fun isInVoiceChannels(voiceChannels: List<VoiceChannel>, member: Member): Boolean {
		return voiceChannels.any { voiceChannel -> voiceChannel.members.any { it.id.equals(member.id, ignoreCase = true) } }
	}

}
