package net.simplyrin.pandaentus.commands.youtube

import java.awt.Color

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

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
class YouTubeLoopCommand : BaseCommand() {

	override val command = "!loop"

	override val description = "再生中の曲をループ"

	override val commandData: CommandData
		get() = CommandDataImpl("loop", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val guildId = event.guild.idLong

		val previousTrack = instance.previousTrack[guildId]
		if (instance.loopMap[guildId] != null) {
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("🔁 ループ再生を無効にしました。")

			instance.loopMap.remove(guildId)
			instance.previousTrack.remove(guildId)
		} else if (previousTrack != null) {
			embedBuilder.setColor(Color.GREEN)
			embedBuilder.setDescription("🔁 ループ再生を有効にしました。")

			instance.loopMap[guildId] = previousTrack
		} else {
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("ループ再生できる曲がありません。")
		}

		event.reply(embedBuilder.build())
	}

}
