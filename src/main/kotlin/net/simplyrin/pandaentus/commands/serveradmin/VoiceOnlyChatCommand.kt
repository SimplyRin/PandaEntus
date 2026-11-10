package net.simplyrin.pandaentus.commands.serveradmin

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

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
class VoiceOnlyChatCommand : BaseCommand() {

	override val command = "!voiceonlychat"

	override val description = "通話に参加しているユーザーのみが送信することのできる専用のテキストチャンネルを作成します。"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.ServerAdministrator

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val guild = event.guild

		if (args.size > 1) {
			if (args[1].equals("create", ignoreCase = true)) {
				if (args.size > 2) {
					val channel = guild.createTextChannel(args[2]).complete()

					instance.config.set("VoiceOnlyChat." + guild.id + ".ID", channel.id)
					event.reply("通話参加者専用テキストチャンネルを作成しました。( " + channel.asMention + " )\n" +
						"デフォルトではユーザーがチャンネルへのメッセージを送信できないよう権限を設定してください。")
					return
				}

				event.reply("使用方法: " + this.command + " create <チャンネル名>")
				return
			}

			if (args[1].equals("delete", ignoreCase = true)) {
				val id: String? = instance.config.getString("VoiceOnlyChat." + guild.id + ".ID", null)
				val channel = if (id.isNullOrEmpty()) null else guild.getTextChannelById(id)
				if (channel == null) {
					event.reply("このサーバーでは、通話参加者専用テキストチャンネルがまだ作成されていません。\n" +
						this.command + " を使用してチャンネルを作成してください。")
					return
				}

				channel.delete().complete()

				instance.config.set("VoiceOnlyChat." + guild.id + ".Role", null)
				instance.config.set("VoiceOnlyChat." + guild.id + ".ID", null)
				event.reply("通話参加者専用テキストチャンネルを削除しました。")
				return
			}
		}

		event.reply("使用方法: " + this.command + " <create|delete> [チャンネル名]")
	}

	fun join(instance: PandaEntus, member: Member) {
		this.updateSendPermission(instance, member, true)
	}

	fun quit(instance: PandaEntus, member: Member) {
		this.updateSendPermission(instance, member, false)
	}

	/**
	 * 通話参加者専用テキストチャンネルでの送信権限を許可/拒否する
	 */
	private fun updateSendPermission(instance: PandaEntus, member: Member, allowed: Boolean) {
		val guild = member.guild
		val id: String = instance.config.getString("VoiceOnlyChat." + guild.id + ".ID", null) ?: return

		val channel = guild.getTextChannelById(id) ?: return

		val override = channel.getPermissionOverride(member)

		if (override != null) {
			val manager = override.manager
			if (allowed) {
				manager.setAllowed(Permission.MESSAGE_SEND).complete()
			} else {
				manager.setDenied(Permission.MESSAGE_SEND).complete()
			}
		} else {
			val action = channel.upsertPermissionOverride(member)
			if (allowed) {
				action.setAllowed(Permission.MESSAGE_SEND).complete()
			} else {
				action.setDenied(Permission.MESSAGE_SEND).complete()
			}
		}
	}

}
