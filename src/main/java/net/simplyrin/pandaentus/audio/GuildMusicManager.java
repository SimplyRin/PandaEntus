package net.simplyrin.pandaentus.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.entities.Guild;
import net.simplyrin.pandaentus.PandaEntus;

/**
 * Holder for both the player and a track scheduler for one guild.
 */
public class GuildMusicManager {
	
	private PandaEntus instance;
	private Guild guild;

	/**
	 * Audio player for the guild.
	 */
	public final AudioPlayer player;
	/**
	 * Track scheduler for the player.
	 */
	public final TrackScheduler scheduler;

	/**
	 * Creates a player and a track scheduler.
	 * @param manager Audio player manager to use for creating the player.
	 */
	public GuildMusicManager(PandaEntus instance, Guild guild, AudioPlayerManager manager) {
		this.instance = instance;
		this.guild = guild;

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
