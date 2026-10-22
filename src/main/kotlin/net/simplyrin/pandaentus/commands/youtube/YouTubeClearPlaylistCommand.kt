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

class YouTubeClearPlaylistCommand : BaseCommand() {

	override val command = "!clearplaylist"

	override val description = "プレイリストを削除"

	override val commandData: CommandData
		get() = CommandDataImpl("clearplaylist", this.description)

	override val aliases = listOf("!cp")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val musicManager = instance.getGuildAudioPlayer(event.guild)
		musicManager.scheduler.clearPlaylist()

		embedBuilder.setColor(Color.GREEN)
		embedBuilder.setDescription("プレイリストを削除しました。")

		event.reply(embedBuilder.build())
	}

}
