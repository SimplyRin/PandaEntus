package net.simplyrin.pandaentus.commands.youtube

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

/**
 * Created by SimplyRin on 2020/07/17.
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
class YouTubeVolumeCommand : BaseCommand() {

	override val command = "!volume"

	override val description = "ボリュームを調節"

	override val commandData: CommandData
		get() = CommandDataImpl("volume", this.description)
			.addOption(OptionType.INTEGER, "音量", "1-100 で音量を調節", true)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val arguments = if (event.isSlashCommand()) {
			arrayOf(this.command, event.slashCommandEvent?.getOption("音量")?.asString.orEmpty())
		} else {
			args
		}

		if (arguments.size > 1) {
			val volume = arguments[1].toIntOrNull()
			if (volume != null && volume in 1..100) {
				instance.config.set("Guild." + event.guild.id + ".Voice-Volume", volume)

				val musicManager = instance.getGuildAudioPlayer(event.guild)
				musicManager.player.volume = volume

				event.reply("ボリュームを $volume に変更しました。")
				return
			}
		}

		event.reply("使用方法: " + this.command + " <1-100>")
	}

}
