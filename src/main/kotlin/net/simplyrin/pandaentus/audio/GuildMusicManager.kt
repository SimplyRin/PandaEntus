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

	init {
		this.player.addListener(this.scheduler)
	}

	/**
	 * Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	val sendHandler: AudioPlayerSendHandler
		get() = AudioPlayerSendHandler(this.player)

}
