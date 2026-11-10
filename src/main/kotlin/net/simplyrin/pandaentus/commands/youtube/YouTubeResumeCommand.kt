package net.simplyrin.pandaentus.commands.youtube

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
class YouTubeResumeCommand : BaseCommand() {

	override val command = "!resume"

	override val description = "一時停止してる曲を再生"

	override val commandData: CommandData
		get() = CommandDataImpl("resume", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val guild = event.guild

		val musicManager = instance.getGuildAudioPlayer(guild)

		val voiceChannel = event.member.voiceState?.channel
		if (voiceChannel == null) {
			event.reply("ボイスチャンネルに接続してください。")
			return
		}

		val audioTrack = musicManager.player.playingTrack
		if (audioTrack == null) {
			event.reply("現在何も再生していません。\n" + instance.commandNameOf(YouTubePlayCommand::class.java) + " コマンドを利用して音楽を再生することができます。")
			return
		}

		musicManager.player.isPaused = false
		event.reply("一時停止している曲を再生します。")

		musicManager.scheduler.updateVoiceStatus(audioTrack)
	}

}
