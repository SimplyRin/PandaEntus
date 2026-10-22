package net.simplyrin.pandaentus.audio

import java.nio.ByteBuffer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame

import net.dv8tion.jda.api.audio.AudioSendHandler

class AudioPlayerSendHandler(val audioPlayer: AudioPlayer) : AudioSendHandler {

	val buffer: ByteBuffer = ByteBuffer.allocate(1024)
	val frame = MutableAudioFrame()

	init {
		this.frame.setBuffer(this.buffer)
	}

	override fun canProvide(): Boolean {
		// returns true if audio was provided
		return this.audioPlayer.provide(this.frame)
	}

	override fun provide20MsAudio(): ByteBuffer {
		// flip to make it a read buffer
		this.buffer.flip()
		return this.buffer
	}

	override fun isOpus(): Boolean {
		return true
	}

}
