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
 * Created by SimplyRin on 2020/07/11.
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
class YouTubeQueueCommand : BaseCommand() {

	override val command = "!queue"

	override val description = "次に再生される曲を確認"

	override val commandData: CommandData
		get() = CommandDataImpl("queue", this.description)

	override val aliases = listOf("!playlist")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val guild = event.guild

		val musicManager = instance.getGuildAudioPlayer(guild)
		val queue = musicManager.scheduler.queue

		val playCommand = instance.commandNameOf(YouTubePlayCommand::class.java)
		val skipCommand = instance.commandNameOf(YouTubeSkipCommand::class.java)
		val loopCommand = instance.commandNameOf(YouTubeLoopCommand::class.java)
		val shuffleCommand = instance.commandNameOf(YouTubeShuffleCommand::class.java)

		val at = instance.loopMap[guild.idLong]
		if (at != null) {
			embedBuilder.setColor(Color.CYAN)
			embedBuilder.setAuthor("🔁 ループ再生が有効になっています。")
			embedBuilder.setDescription("🎵 再生中の音楽: " + at.info.title)
			embedBuilder.setFooter("詳細: !nowplaying, ループ無効: $loopCommand")

			event.reply(embedBuilder.build())
			return
		}

		if (queue.isEmpty()) {
			event.reply("次に再生が予定されている曲はありません。\n$playCommand を使用することで、プレイリストに追加することができます。")
			return
		}

		var message = ""

		val playingTrack = musicManager.player.playingTrack
		if (playingTrack != null) {
			embedBuilder.setAuthor("🎵 再生中の音楽: " + playingTrack.info.title)
		}

		var i = 1
		for (audioTrack in queue) {
			message += "**$i:** __" + audioTrack.info.title + "__\n"
			i++
		}

		if (message.isEmpty()) {
			event.reply("再生待ちの曲はありません。")
			return
		}

		if (message.length >= 1800) {
			message = message.substring(0, 1800)
		}

		embedBuilder.setDescription(message)
		embedBuilder.setColor(Color.CYAN)
		embedBuilder.setFooter("追加: $playCommand, スキップ: $skipCommand, シャッフル: $shuffleCommand")

		event.reply(embedBuilder.build())
	}

}
