package net.simplyrin.pandaentus.listeners

import java.awt.Color
import java.io.File

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.utils.ThreadPool

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

		// YouTube の動画 ID。これ以外の文字列は yt-dlp に渡さない (オプションとして解釈される文字列を防ぐ)
		private val VIDEO_ID = Regex("^[A-Za-z0-9_-]{11}$")

		private const val LOADING_URL = "https://static.simplyrin.net/gif/loading.gif?id=1"
		private const val DOWNLOADING_URL = "https://static.simplyrin.net/gif/download.gif?id=1"
		private const val UPLOADING_URL = "https://static.simplyrin.net/gif/upload.gif?id=1"

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

		val videoId = tempVideoId.trim()

		println("[YouTube Download Task] Original: $url, Repalced: $videoId")

		if (!VIDEO_ID.matches(videoId)) {
			println("[YouTube Download Task] 動画 ID として扱えないため、中止しました。")
			return
		}

		val folder = File("ytdl")
		folder.mkdirs()

		val mp3 = File(folder, "$videoId.mp3")

		// yt-dlp の実行には時間がかかるので、JDA のイベント用スレッドでは実行しない
		ThreadPool.run {
			try {
				if (mp3.exists()) {
					this.sendCached(channel, videoId, mp3)
				} else {
					this.download(channel, user, videoId, folder, mp3)
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	private fun sendCached(channel: MessageChannel, videoId: String, mp3: File) {
		val embedBuilder = this.createReadyEmbed(videoId)

		val tempMessage = channel.sendFiles(FileUpload.fromData(mp3)).setEmbeds(embedBuilder.build()).complete()

		try {
			Thread.sleep(1000L * 60 * 60 * 48)
		} catch (e: Exception) {
		}

		tempMessage.delete().complete()
	}

	private fun download(channel: MessageChannel, user: User, videoId: String, folder: File, mp3: File) {
		val tools = this.instance.externalTools
		val videoUrl = "https://www.youtube.com/watch?v=$videoId"

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)
		embedBuilder.setAuthor(user.name + " -> 処理中...", null, LOADING_URL)

		val phase = channel.sendMessageEmbeds(embedBuilder.build()).complete()

		// タイトル、長さ (秒)、長さ (表示用) を 1 行ずつ出力させる
		val info = tools.runYtDlp(listOf(
			"--print", "%(title)s", "--print", "%(duration)s", "--print", "%(duration_string)s",
			"--", videoUrl), 120)
		if (info == null) {
			this.fail(phase, user, "yt-dlp を使用できません。")
			return
		}
		if (!info.success || info.stdout.size < 3) {
			println("[YouTube Download Task] 動画情報の取得に失敗しました: " + info.describeFailure())
			this.fail(phase, user, "動画の情報を取得できませんでした。")
			return
		}

		val title = info.stdout[0].trim()
		val seconds = info.stdout[1].trim().toDoubleOrNull()
		val duration = info.stdout[2].trim()

		println("[YouTube Download Task] Title: $title, Duration: $duration (${seconds}s, Max: " +
			this.instance.formatMillis(MAX_DURATION_SECONDS * 1000L) + ")")

		// ライブ配信などは長さを取得できない
		if (seconds == null) {
			this.fail(phase, user, "動画の長さを取得できませんでした。")
			return
		}
		if (seconds >= MAX_DURATION_SECONDS) {
			this.fail(phase, user, "動画が長すぎます！")
			return
		}

		val data = this.instance.dataConfig
		synchronized(data) {
			data.set("YouTube.$videoId.Title", title)
			data.set("YouTube.$videoId.Duration", duration)
		}

		embedBuilder.setAuthor(user.name + " -> ダウンロードしています...", null, DOWNLOADING_URL)
		phase.editMessageEmbeds(embedBuilder.build()).complete()

		val result = tools.runYtDlp(listOf(
			"-f", "bestaudio", "-x", "--audio-format", "mp3",
			"--output", File(folder, "$videoId.%(ext)s").absolutePath,
			"--", videoUrl), 600)
		if (result == null || !result.success || !mp3.exists()) {
			println("[YouTube Download Task] ダウンロードに失敗しました: " + (result?.describeFailure() ?: "yt-dlp を起動できません"))
			this.fail(phase, user, "ダウンロードに失敗しました。")
			return
		}

		synchronized(data) {
			data.set("YouTube.$videoId.Path", mp3.absolutePath)
		}

		embedBuilder.setAuthor(user.name + " -> 送信しています...", null, UPLOADING_URL)
		phase.editMessageEmbeds(embedBuilder.build()).complete()

		val readyEmbedBuilder = this.createReadyEmbed(videoId)

		channel.sendFiles(FileUpload.fromData(mp3)).setEmbeds(readyEmbedBuilder.build()).complete()
		phase.delete().complete()
	}

	private fun fail(phase: Message, user: User, reason: String) {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.RED)
		embedBuilder.setAuthor(user.name + " -> " + reason)
		phase.editMessageEmbeds(embedBuilder.build()).complete()
	}

	/**
	 * ダウンロードしたファイルの案内用 Embed を作成する
	 */
	private fun createReadyEmbed(videoId: String): EmbedBuilder {
		val title: String? = this.instance.dataConfig.getString("YouTube.$videoId.Title")
		val duration: String? = this.instance.dataConfig.getString("YouTube.$videoId.Duration")

		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.GREEN)
		embedBuilder.setAuthor("ファイルが準備できました。")
		embedBuilder.addField("タイトル", title.takeUnless { it.isNullOrEmpty() } ?: "不明", true)
		embedBuilder.addField("長さ", duration.takeUnless { it.isNullOrEmpty() } ?: "不明", true)
		return embedBuilder
	}

}
