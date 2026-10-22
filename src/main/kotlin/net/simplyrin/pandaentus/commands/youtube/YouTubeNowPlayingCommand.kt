package net.simplyrin.pandaentus.commands.youtube

import java.awt.Color
import java.math.BigDecimal
import java.math.RoundingMode

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
class YouTubeNowPlayingCommand : BaseCommand() {

	override val command = "!nowplaying"

	override val description = "現在再生中の曲を確認"

	override val commandData: CommandData
		get() = CommandDataImpl("nowplaying", this.description)

	override val aliases = listOf("!np")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val guild = event.guild

		val musicManager = instance.getGuildAudioPlayer(guild)

		val audioTrack = musicManager.player.playingTrack
		if (audioTrack == null) {
			event.reply("現在何も再生していません。\n" + instance.commandNameOf(YouTubePlayCommand::class.java) + " コマンドを利用して音楽を再生することができます。")
			return
		}

		val position = audioTrack.position / 1000.0
		val duration = audioTrack.duration / 1000.0

		// 長さが不明なトラック (ライブ配信など) は 0% として扱う
		val progress = if (duration > 0) {
			BigDecimal((position / duration) * 100).setScale(2, RoundingMode.FLOOR).toDouble()
		} else {
			0.0
		}
		val percent = "$progress%"

		val barLength = 20
		val barPos = (progress / 5).toInt().coerceIn(0, barLength - 1)
		val bar = "　".repeat(barLength).replaceRange(barPos, barPos + 1, "●")

		println("▶︎ $bar :$position / $duration = $percent")

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)

		var prefix = if (instance.loopMap[guild.idLong] != null) "🔁" else "▶"
		if (musicManager.player.isPaused) {
			prefix = "⏸"
		}

		embedBuilder.setDescription(prefix + " " + instance.formatMillis(audioTrack.position) + " / "
			+ instance.formatMillis(audioTrack.duration) + ": ~~" + bar + "~~")

		embedBuilder.addField("🎵 再生中の音楽", audioTrack.info.title, false)
		embedBuilder.addField("💿 アーティスト/チャンネル", audioTrack.info.author, false)
		embedBuilder.addField("🔗 リンク", audioTrack.info.uri, false)

		println(audioTrack.info.identifier)

		event.reply(embedBuilder.build())
	}

}
