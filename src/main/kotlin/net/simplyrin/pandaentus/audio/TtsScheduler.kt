package net.simplyrin.pandaentus.audio

import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason

import net.simplyrin.pandaentus.tts.TtsManager

/**
 * 読み上げ用プレイヤーのキュー。再生が終わった音声ファイルは削除する。
 */
class TtsScheduler(private val player: AudioPlayer) : AudioEventAdapter() {

	private val queue = ConcurrentLinkedQueue<AudioTrack>()

	/**
	 * 再生待ちの数
	 */
	val pendingCount: Int
		get() = this.queue.size

	/**
	 * 何も再生していなければすぐに再生し、再生中であればキューの末尾に追加する。
	 */
	fun queue(track: AudioTrack) {
		if (this.player.startTrack(track, true)) {
			return
		}

		this.queue.offer(track)

		// 追加する間に再生が終わっていた場合に、キューに残ったままにならないようにする
		if (this.player.playingTrack == null) {
			val next = this.queue.poll()
			if (next != null) {
				this.player.startTrack(next, true)
			}
		}
	}

	/**
	 * 再生中と再生待ちの読み上げを全て破棄する。
	 */
	fun clear() {
		for (track in this.queue) {
			deleteFile(track)
		}
		this.queue.clear()

		this.player.stopTrack()
	}

	override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
		deleteFile(track)

		// 正常に終わった (または読み込みに失敗した) ときは、次の読み上げを再生する
		if (endReason.mayStartNext) {
			val next = this.queue.poll()
			if (next != null) {
				player.startTrack(next, false)
			}
		}
	}

	/**
	 * 読み上げ用に生成した音声ファイルを削除する。tts フォルダの外のファイルは削除しない。
	 */
	private fun deleteFile(track: AudioTrack) {
		val file = File(track.identifier).absoluteFile
		if (file.parentFile == TtsManager.FOLDER.absoluteFile) {
			file.delete()
		}
	}

}
