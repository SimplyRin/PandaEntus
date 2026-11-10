package net.simplyrin.pandaentus.commands.youtube

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.classes.ReactionMessage

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
class YouTubeDownloadCommand : BaseCommand() {

	override val command = "https://www.youtube.com/watch?v="

	override val aliases = listOf("https://youtu.be/", "https://soundcloud.com/")

	override fun sendTyping(): Boolean {
		return false
	}

	override val type = CommandType.StartsWith

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val message = args.drop(1).joinToString(" ").trim()

		val splitSize = message.split("/").size
		println(splitSize)

		if (message.startsWith("https://soundcloud.com/")) {
			println(splitSize)
		}

		if (message.startsWith("https://soundcloud.com/") && !message.contains("-") && splitSize <= 4) {
			return
		}

		// "download" という名前の絵文字があればそれを、なければサーバーの最初の絵文字を使う
		val emojis = event.guild.emojis
		val emote = emojis.lastOrNull { it.name == "download" } ?: emojis.firstOrNull() ?: return

		val targetMessage = event.message ?: return
		targetMessage.addReaction(emote).complete()

		instance.messages.add(ReactionMessage(targetMessage, emote))
	}

}
