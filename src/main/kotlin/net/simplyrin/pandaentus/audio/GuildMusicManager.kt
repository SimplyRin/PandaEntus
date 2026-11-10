package net.simplyrin.pandaentus.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

import net.dv8tion.jda.api.entities.Guild
import net.simplyrin.pandaentus.PandaEntus

class GuildMusicManager(instance: PandaEntus, guild: Guild, manager: AudioPlayerManager) {

	/**
	 * Audio player for the guild.
	 */
	val player: AudioPlayer = manager.createPlayer()

	/**
	 * Track scheduler for the player.
	 */
	val scheduler: TrackScheduler = TrackScheduler(instance, guild, this.player)

	/**
	 * Audio player for text-to-speech. Its audio takes priority over the music player.
	 */
	val ttsPlayer: AudioPlayer = manager.createPlayer()

	/**
	 * Queue for the text-to-speech player.
	 */
	val ttsScheduler: TtsScheduler = TtsScheduler(this.ttsPlayer)

	init {
		this.player.addListener(this.scheduler)
		this.ttsPlayer.addListener(this.ttsScheduler)
	}

	/**
	 * Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	val sendHandler: AudioPlayerSendHandler
		get() = AudioPlayerSendHandler(this.player, this.ttsPlayer)

}
