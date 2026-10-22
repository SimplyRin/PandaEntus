package net.simplyrin.pandaentus.audio

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.simplyrin.pandaentus.PandaEntus

class TrackScheduler(
	private val instance: PandaEntus,
	private val guild: Guild,
	val player: AudioPlayer
) : AudioEventAdapter() {

	val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	fun queue(track: AudioTrack) {
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.
		if (!this.player.startTrack(track, true)) {
			this.queue.offer(track)
		} else {
			this.updateVoiceStatus(track)
		}
	}

	fun shuffle() {
		val list = this.queue.toMutableList()
		list.shuffle()

		// クリア
		this.clearPlaylist()

		this.queue.addAll(list)
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	fun nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		val track = this.queue.poll()

		this.player.startTrack(track, false)

		this.updateVoiceStatus(track)
	}

	enum class TrackStatus {
		PLAY, PAUSE, END
	}

	fun updateVoiceStatus(track: AudioTrack?) {
		this.updateVoiceStatus(track, TrackStatus.PLAY)
	}

	fun updateVoiceStatus(track: AudioTrack?, status: TrackStatus) {
		val emoji = when (status) {
			TrackStatus.PLAY -> ":arrow_forward:"
			TrackStatus.PAUSE -> ":pause_button:"
			TrackStatus.END -> ""
		}

		val voiceChannel = this.findJoinedVoiceChannel()
		if (voiceChannel != null) {
			if (track != null) {
				voiceChannel.modifyStatus(emoji + " " + track.info.title).complete()
			} else {
				voiceChannel.modifyStatus("").complete()
			}
		}
	}

	/**
	 * Clear playlist
	 */
	fun clearPlaylist() {
		this.queue.clear()
	}

	override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (this.instance.loopMap[this.guild.idLong] != null) {
			val voiceChannel = this.findJoinedVoiceChannel()

			println("onTrackEnd -> voiceChannel : $voiceChannel")

			if (voiceChannel != null) {
				if (voiceChannel.members.size >= 1) {
					player.startTrack(track.makeClone(), false)
				} else if (this.instance.forceLoopMap[this.guild.idLong] != null) {
					val forceLoopTrack = this.instance.forceLoopMap[this.guild.idLong]!!

					player.startTrack(forceLoopTrack.makeClone(), false)
				}
			}
		} else if (endReason.mayStartNext) {
			this.nextTrack()
		} else {
			this.updateVoiceStatus(null)
		}
	}

	/**
	 * Bot が参加しているボイスチャンネルを返す
	 */
	private fun findJoinedVoiceChannel(): VoiceChannel? {
		val selfId = this.instance.jda.selfUser.id

		var voiceChannel: VoiceChannel? = null
		for (vc in this.guild.voiceChannels) {
			for (member in vc.members) {
				if (member.user.id == selfId) {
					voiceChannel = vc
				}
			}
		}
		return voiceChannel
	}

}
