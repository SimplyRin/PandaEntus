package net.simplyrin.pandaentus.commands.youtube

import java.awt.Color

import com.google.common.reflect.ClassPath
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

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
class YouTubeHelpCommand : BaseCommand() {

	override val command = "!ythelp"

	override val description = "Music Bot のヘルプを表示"

	override val commandData: CommandData
		get() = CommandDataImpl("ythelp", this.description)

	override val aliases = listOf("!youtube")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = this.getHelpEmbed(instance) ?: return
		event.reply(embedBuilder.build())
	}

	fun getHelpMessage(): String {
		return "🎵 Music Bot 機能コマンド一覧 / ヘルプ"
	}

	fun getHelpEmbed(instance: PandaEntus): EmbedBuilder? {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setAuthor(this.getHelpMessage())
		embedBuilder.setColor(Color.CYAN)

		try {
			val classLoader = Thread.currentThread().contextClassLoader
			for (classInfo in ClassPath.from(classLoader).topLevelClasses) {
				if (classInfo.name.startsWith(YouTubeHelpCommand::class.java.packageName)) {
					val command = instance.commandRegister.getRegisteredCommand(Class.forName(classInfo.name)) ?: continue
					val description = command.description ?: continue

					val aliases = command.aliases?.takeIf { it.isNotEmpty() }?.joinToString(", ")

					embedBuilder.addField(command.command + (if (aliases != null) " ($aliases)" else ""), description, true)
				}
			}
		} catch (e: Exception) {
			instance.postError(e)
			return null
		}

		return embedBuilder
	}

}
