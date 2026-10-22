package net.simplyrin.pandaentus.commands.youtube

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

class YouTubeShuffleCommand : BaseCommand() {

	override val command = "!shuffle"

	override val description = "プレイリストをシャッフル"

	override val commandData: CommandData
		get() = CommandDataImpl("shuffle", this.description)

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

		musicManager.scheduler.shuffle()
		event.reply("プレイリストをシャッフルしました。")
	}

}
