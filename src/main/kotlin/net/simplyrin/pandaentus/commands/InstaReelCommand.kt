package net.simplyrin.pandaentus.commands

import java.io.File
import java.util.UUID

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.processmanager.Callback
import net.simplyrin.processmanager.ProcessManager

/**
 * Created by SimplyRin on 2020/11/25.
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
class InstaReelCommand : BaseCommand() {

	override val command = "https://www.instagram.com/reel/"

	override fun sendTyping(): Boolean {
		return false
	}

	override val type = CommandType.StartsWith

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val url = args.firstOrNull().orEmpty().substringBefore("?")

		val id = UUID.randomUUID().toString().split("-")[0]
		println("[InstaReelCommand] [$id] Checking: $url")

		var youtubeDlPathTemp = "youtube-dl"
		val youtubeDl = File("youtube-dl")
		if (youtubeDl.exists()) {
			youtubeDlPathTemp = youtubeDl.absolutePath
		}

		ProcessManager.runCommand(arrayOf(youtubeDlPathTemp, "--get-url", url), object : Callback() {

			override fun line(response: String?) {
				if (response == null) {
					return
				}

				println("[InstaReel] [$id] $response")

				if (response.lowercase().startsWith("https://")) {
					event.channel.sendMessage(response).complete()
				}
			}

		}, true)
	}

}
