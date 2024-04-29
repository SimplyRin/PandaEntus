package net.simplyrin.pandaentus.audio;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.simplyrin.pandaentus.PandaEntus;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
	
	private PandaEntus instance;
	private Guild guild;
	
	@Getter
	private final AudioPlayer player;
	@Getter
	private final BlockingQueue<AudioTrack> queue;

	/**
	 * @param player The audio player this scheduler uses
	 */
	public TrackScheduler(PandaEntus instance, Guild guild, AudioPlayer player) {
		this.instance = instance;
		this.guild = guild;
		
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTra^
		// ck with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.
		if (!this.player.startTrack(track, true)) {
			this.queue.offer(track);
		} else {
			this.updateVoiceStatus(track);
		}
	}

	public void shuffle() {
		AudioTrack[] array = this.queue.toArray(new AudioTrack[0]);
		Collections.shuffle(Arrays.asList(array));

		// クリア
		this.clearPlaylist();

		Collections.addAll(this.queue, array);
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		var track = this.queue.poll();

		this.player.startTrack(track, false);

        this.updateVoiceStatus(track);
    }

	public void updateVoiceStatus(AudioTrack track) {
		if (track != null) {
			VoiceChannel voiceChannel = null;
			for (VoiceChannel vc : this.guild.getVoiceChannels()) {
				for (Member member : vc.getMembers()) {
					User user = member.getUser();
					User selfUser = this.instance.getJda().getSelfUser();

					if (user.getId().equals(selfUser.getId())) {
						voiceChannel = vc;
					}
				}
			}

			if (voiceChannel != null) {
				voiceChannel.modifyStatus("🎵　" + track.getInfo().title).complete();
			}
		}

		VoiceChannel voiceChannel = null;
		for (VoiceChannel vc : this.guild.getVoiceChannels()) {
			for (Member member : vc.getMembers()) {
				User user = member.getUser();
				User selfUser = this.instance.getJda().getSelfUser();

				if (user.getId().equals(selfUser.getId())) {
					voiceChannel = vc;
				}
			}
		}

		if (voiceChannel != null) {
			if (track != null) {
				voiceChannel.modifyStatus("🎵 " + track.getInfo().title).complete();
			} else {
				voiceChannel.modifyStatus("").complete();
			}
		}
	}

	/**
	 * Clear playlist
	 */
	public void clearPlaylist() {
		this.queue.clear();
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (this.instance.getLoopMap().get(this.guild.getIdLong()) != null) {
			VoiceChannel voiceChannel = null;
			for (VoiceChannel vc : this.guild.getVoiceChannels()) {
				for (Member member : vc.getMembers()) {
					User user = member.getUser();
					User selfUser = this.instance.getJda().getSelfUser();

					if (user.getId().equals(selfUser.getId())) {
						voiceChannel = vc;
					}
				}
			}
			
			System.out.println("onTrackEnd -> voiceChannel : " + voiceChannel);
			
			if (voiceChannel != null) {
				if (voiceChannel.getMembers().size() >= 1) {
					player.startTrack(track.makeClone(), false);
					voiceChannel.modifyStatus("🎵 " + track.getInfo().title).complete();
					return;
				}
				
				else if (this.instance.getForceLoopMap().get(this.guild.getIdLong()) != null) {
					var forceLoopTrack = this.instance.getForceLoopMap().get(this.guild.getIdLong());
					
					player.startTrack(forceLoopTrack.makeClone(), false);
					voiceChannel.modifyStatus("🎵 " + forceLoopTrack.getInfo().title).complete();
				}
			}
		} else if (endReason.mayStartNext) {
			this.nextTrack();
		} else {
			this.updateVoiceStatus(null);
		}
	}

}