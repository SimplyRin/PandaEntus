package net.simplyrin.pandaentus.listeners

import java.awt.Color
import java.io.File

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.utils.ThreadPool
import net.simplyrin.processmanager.Callback
import net.simplyrin.processmanager.ProcessManager

/**
 * Created by SimplyRin on 2020/06/21.
 *
 * Copyright (C) 2020 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
class ReactionListener(private val instance: PandaEntus) : ListenerAdapter() {

	companion object {

		// ダウンロードできる動画の最大の長さ (秒)
		private const val MAX_DURATION_SECONDS = 60 * 8

	}

	override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
		val user = event.user ?: return
		if (user.isBot) {
			return
		}

		val reactionMessage = this.instance.messages.lastOrNull { it.message.id == event.messageId } ?: return

		if (event.reaction.emoji.name != reactionMessage.emote.name) {
			return
		}

		val message = reactionMessage.message

		this.instance.messages.remove(reactionMessage)

		val channel = message.channel

		val url = message.contentRaw

		var tempVideoId = url
		if (tempVideoId.contains("v=")) {
			tempVideoId = tempVideoId.split("v=").getOrElse(1) { tempVideoId }
			tempVideoId = tempVideoId.split("&")[0]
		}

		tempVideoId = tempVideoId.replace("https://www.youtube.com/watch?v=", "")
		tempVideoId = tempVideoId.replace("https://youtu.be/", "")
		tempVideoId = tempVideoId.replace("http://youtu.be/", "")
		tempVideoId = tempVideoId.replace("http://www.youtube.com/watch?v=", "")

		tempVideoId = tempVideoId.split("&")[0]
		tempVideoId = tempVideoId.split("?")[0]

		val videoId = tempVideoId

		println("[YouTube Download Task] Original: $url, Repalced: $videoId")

		val folder = File("ytdl")
		folder.mkdirs()

		val mp3 = File(folder, "$videoId.mp3")

		if (mp3.exists()) {
			ThreadPool.run {
				val embedBuilder = this.createReadyEmbed(videoId)

				val tempMessage = channel.sendFiles(FileUpload.fromData(mp3)).setEmbeds(embedBuilder.build()).complete()

				try {
					Thread.sleep(1000L * 60 * 60 * 48)
				} catch (e: Exception) {
				}

				tempMessage.delete().complete()
			}
			return
		}

		val loadingUrl = "https://static.simplyrin.net/gif/loading.gif?id=1"
		val downloadingUrl = "https://static.simplyrin.net/gif/download.gif?id=1"
		val uploadingUrl = "https://static.simplyrin.net/gif/upload.gif?id=1"

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)
		embedBuilder.setAuthor(user.name + " -> 処理中...", null, loadingUrl)

		val phase = channel.sendMessageEmbeds(embedBuilder.build()).complete()

		var youtubeDlPath = "youtube-dl"
		val youtubeDl = File("youtube-dl")
		if (youtubeDl.exists()) {
			youtubeDlPath = youtubeDl.absolutePath
		}

		ProcessManager.runCommand(arrayOf(youtubeDlPath, "--get-title", videoId), object : Callback() {

			override fun line(response: String?) {
				if (response != null && !response.lowercase().startsWith("error:")) {
					println("[YouTube Download Task] Title: $response")
					instance.config.set("YouTube.$videoId.Title", response.trim())
				}
			}

		}, true)

		ProcessManager.runCommand(arrayOf(youtubeDlPath, "--get-duration", videoId), object : Callback() {

			override fun line(response: String?) {
				if (response == null) {
					return
				}

				if (response.contains(":")) {
					println("[YouTube Download Task] Duration: $response")
					instance.config.set("YouTube.$videoId.Duration", response.trim())
				}

				val seconds = parseDuration(response.trim())

				println("[YouTube Download Task] Seconds: ${seconds}s (Duration: ${response.trim()}, Max: " +
					instance.formatMillis(MAX_DURATION_SECONDS * 1000L) + ")")

				if (seconds >= MAX_DURATION_SECONDS) {
					embedBuilder.setColor(Color.RED)
					embedBuilder.setAuthor(user.name + " -> 動画が長すぎます！", null, loadingUrl)
					phase.editMessageEmbeds(embedBuilder.build()).complete()
					return
				}

				embedBuilder.setAuthor(user.name + " -> ダウンロードしています...", null, downloadingUrl)
				phase.editMessageEmbeds(embedBuilder.build()).complete()

				ProcessManager.runCommand(arrayOf(youtubeDlPath, "-f", "bestaudio", "--audio-format", "mp3", "--no-playlist",
					"--output", "./ytdl/$videoId.%(ext)s", "-x", videoId), object : Callback() {

					override fun line(response: String?) {
						if (response != null && response.startsWith("[ffmpeg] Destination:")) {
							val title = response.replace("[ffmpeg] Destination:", "").replace("\"", "").trim()
							println("[YouTube Download Task] Filename: $title")
						}
					}

					override fun processEnded() {
						instance.config.set("YouTube.$videoId.Path", mp3.absolutePath)

						embedBuilder.setAuthor(user.name + " -> 送信しています...", null, uploadingUrl)
						phase.editMessageEmbeds(embedBuilder.build()).complete()

						if (mp3.exists()) {
							val readyEmbedBuilder = createReadyEmbed(videoId)

							channel.sendFiles(FileUpload.fromData(mp3)).setEmbeds(readyEmbedBuilder.build()).complete()
							phase.delete().complete()
						}
					}

				}, true)
			}

		}, true)
	}

	/**
	 * ダウンロードしたファイルの案内用 Embed を作成する
	 */
	private fun createReadyEmbed(videoId: String): EmbedBuilder {
		val title: String? = this.instance.config.getString("YouTube.$videoId.Title")
		val duration: String? = this.instance.config.getString("YouTube.$videoId.Duration")

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)
		embedBuilder.setAuthor("ファイルが準備できました。")
		embedBuilder.addField("タイトル", title.takeUnless { it.isNullOrEmpty() } ?: "不明", true)
		embedBuilder.addField("長さ", duration.takeUnless { it.isNullOrEmpty() } ?: "不明", true)
		return embedBuilder
	}

	/**
	 * "mm:ss" や "h:mm:ss" 形式の長さを秒に変換する。解析できない場合は Int.MAX_VALUE
	 */
	private fun parseDuration(duration: String): Int {
		val parts = duration.split(":").map { it.toIntOrNull() }
		if (parts.isEmpty() || parts.any { it == null }) {
			return Int.MAX_VALUE
		}

		return parts.filterNotNull().fold(0) { total, part -> total * 60 + part }
	}

}
