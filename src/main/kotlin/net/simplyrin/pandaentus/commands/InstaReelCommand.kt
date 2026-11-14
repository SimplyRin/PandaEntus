package net.simplyrin.pandaentus.commands

import java.util.UUID

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.SafeUrl
import net.simplyrin.pandaentus.utils.ThreadPool

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
		val url = SafeUrl.parse(args.firstOrNull().orEmpty().substringBefore("?"), "instagram.com") ?: return

		val id = UUID.randomUUID().toString().split("-")[0]
		println("[InstaReelCommand] [$id] Checking: $url")

		ThreadPool.run {
			try {
				val result = instance.externalTools.runYtDlp(listOf("--get-url", "--", url), 120)
				if (result == null || !result.success) {
					println("[InstaReel] [$id] 取得に失敗しました: " + (result?.describeFailure() ?: "yt-dlp を使用できません"))
					return@run
				}

				for (line in result.stdout) {
					println("[InstaReel] [$id] $line")

					if (line.lowercase().startsWith("https://")) {
						event.channel.sendMessage(line.trim()).complete()
					}
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

}
