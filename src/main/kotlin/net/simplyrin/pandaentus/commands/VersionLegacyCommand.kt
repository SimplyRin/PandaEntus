package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.util.Scanner

import net.dv8tion.jda.api.EmbedBuilder
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.Version

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
class VersionLegacyCommand : BaseCommand() {

	override val command = "!version_legacy"

	override val description = "PandaEntus のバージョンを確認"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)
		embedBuilder.addField("Version:", Version.BUILD_TIME, true)
		embedBuilder.addField("Server uptime", this.getUptime(instance), true)

		event.reply(embedBuilder.build())
	}

	fun getUptime(instance: PandaEntus): String {
		var uptime = "unknown"

		try {
			val process = Runtime.getRuntime().exec(arrayOf("uptime", "-p"))

			Scanner(process.inputStream).use { scanner ->
				if (scanner.hasNext()) {
					uptime = scanner.nextLine()
						.replace("up ", "")
						.replace(",", "")
						.replace(" years", "年")
						.replace(" year", "年")
						.replace(" weeks", "週間")
						.replace(" week", "週間")
						.replace(" days", "日")
						.replace(" day", "日")
						.replace(" hours", "時間")
						.replace(" hour", "時間")
						.replace(" minutes", "分")
						.replace(" minute", "分")
				}
			}
		} catch (e: Exception) {
			instance.postError(e)
		}

		return uptime
	}

}
