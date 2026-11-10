package net.simplyrin.pandaentus.commands

import java.text.SimpleDateFormat

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.Version

/**
 * Created by SimplyRin on 2020/07/13.
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
class VersionCommand : BaseCommand() {

	override val command = "!version"

	override val description = "Bot の詳細情報を表示"

	override val commandData: CommandData
		get() = CommandDataImpl("version", this.description)

	override val aliases = listOf("!stats")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val date = instance.startupDate
		val runtime = Runtime.getRuntime()
		val jda = instance.jda

		val startup = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date)

		val versionLegacyCommand = instance.commandRegister.getRegisteredCommand(VersionLegacyCommand::class.java) as? VersionLegacyCommand
			?: VersionLegacyCommand()

		var message = "```"
		message += "PandaEntus 起動日: $startup\n"
		message += "PandaEntus 稼働日: " + instance.getUptime(date) + "\n\n"

		message += "サーバー起動日: " + versionLegacyCommand.getUptime(instance) + "\n"
		message += "利用可能コア数: " + runtime.availableProcessors() + "\n\n"

		message += "メモリ空き状態:\n"
		message += "  空き状態: " + instance.formatSize(runtime.freeMemory().toDouble()) + "\n"
		message += "  使用容量: " + instance.formatSize(runtime.totalMemory().toDouble()) + "\n"
		message += "  最大容量: " +
			(if (runtime.maxMemory() == Long.MAX_VALUE) "無制限" else instance.formatSize(runtime.maxMemory().toDouble())) + "\n\n"

		message += "サーバー情報:\n"
		message += "  サーバー数: " + jda.guilds.size + "\n"
		message += "  テキストチャンネル数: " + jda.textChannels.size + "\n"
		message += "  ボイスチャンネル数: " + jda.voiceChannels.size + "\n"
		message += "  ユーザー数: " + jda.users.size + "\n\n"

		message += "バージョン情報:\n"
		message += "  Java: " + System.getProperty("java.version") + "\n"
		message += "  PandaEntus: " + Version.POMVERSION + ", Bulid Time: " + Version.BUILD_TIME + ", Git: " +
			(if (Version.SHA == "") "unknown" else Version.SHA) + "\n"
		message += "  JDA: " + JDAInfo.VERSION + "\n"
		message += "  LavaPlayer: " + PlayerLibrary.VERSION
		message += "```"

		event.reply(message)
	}

}
