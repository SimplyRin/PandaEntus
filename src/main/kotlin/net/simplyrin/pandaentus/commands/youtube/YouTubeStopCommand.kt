package net.simplyrin.pandaentus.commands.youtube

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.audio.TrackScheduler
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

class YouTubeStopCommand : BaseCommand() {

	override val command = "!stop"

	override val description = "再生中の曲を停止"

	override val commandData: CommandData
		get() = CommandDataImpl("stop", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel
		val guild = event.guild

		val musicManager = instance.getGuildAudioPlayer(guild)

		val voiceChannel = event.member.voiceState?.channel
		if (voiceChannel == null) {
			channel.sendMessage("ボイスチャンネルに接続してください。").complete()
			return
		}

		val audioTrack = musicManager.player.playingTrack
		if (audioTrack == null) {
			channel.sendMessage("現在何も再生していません。\n" + instance.commandNameOf(YouTubePlayCommand::class.java) + " コマンドを利用して音楽を再生することができます。").complete()
			return
		}

		musicManager.player.stopTrack()
		musicManager.scheduler.clearPlaylist()
		instance.getGuildAudioPlayer(guild).player.destroy()
		guild.audioManager.closeAudioConnection()

		channel.sendMessage("停止しました。").complete()

		musicManager.scheduler.updateVoiceStatus(null, TrackScheduler.TrackStatus.END)
	}

}
