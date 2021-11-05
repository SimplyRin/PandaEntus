package net.simplyrin.pandaentus.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.simplyrin.pandaentus.PandaEntus;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
@Getter
public class GuildMusicManager {
	
	/**
	 * Audio player for the guild.
	 */
	private final AudioPlayer player;
	/**
	 * Track scheduler for the player.
	 */
	private final TrackScheduler scheduler;

	/**
	 * Creates a player and a track scheduler.
	 * @param manager Audio player manager to use for creating the player.
	 */
	public GuildMusicManager(PandaEntus instance, Guild guild, AudioPlayerManager manager) {
		this.player = manager.createPlayer();
		this.scheduler = new TrackScheduler(instance, guild, this.player);
		this.player.addListener(this.scheduler);
	}

	/**
	 * @return Wrapper around AudioPlayer to use it as an AudioSendHandler.
	 */
	public AudioPlayerSendHandler getSendHandler() {
		return new AudioPlayerSendHandler(this.player);
	}
}
