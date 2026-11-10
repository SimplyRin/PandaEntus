package net.simplyrin.pandaentus.audio

import java.nio.ByteBuffer

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame

import net.dv8tion.jda.api.audio.AudioSendHandler

/**
 * @param audioPlayer 通常のプレイヤー (音楽)
 * @param priorityPlayer 優先して送るプレイヤー (読み上げ)。音声を出している間は audioPlayer の音声を取り出さないため、
 * 音楽は一時停止した状態になり、読み上げが終わると続きから再開する。
 */
class AudioPlayerSendHandler(val audioPlayer: AudioPlayer, private val priorityPlayer: AudioPlayer? = null) : AudioSendHandler {

	val buffer: ByteBuffer = ByteBuffer.allocate(1024)
	val frame = MutableAudioFrame()

	init {
		this.frame.setBuffer(this.buffer)
	}

	override fun canProvide(): Boolean {
		// returns true if audio was provided
		if (this.priorityPlayer?.provide(this.frame) == true) {
			return true
		}

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
