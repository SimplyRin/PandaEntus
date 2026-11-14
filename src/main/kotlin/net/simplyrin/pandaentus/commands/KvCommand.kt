package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.Base64
import java.util.UUID

import net.dv8tion.jda.api.EmbedBuilder
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.ThreadPool
import net.simplyrin.pandaentus.utils.VoicePlayer

/**
 * Created by SimplyRin on 2020/07/09.
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
class KvCommand : BaseCommand() {

	companion object {

		private const val ENDPOINT = "https://api.voicetext.jp/v1/tts"

		private val client: HttpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build()

	}

	override val command = "!kv"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val channel = event.channel
		val member = event.member
		val guild = event.guild

		if (member.voiceState?.channel == null) {
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("ボイスチャンネルに接続してください！")
			channel.sendMessageEmbeds(embedBuilder.build()).complete()
			return
		}

		if (args.size > 1) {
			val folder = File("kv")
			folder.mkdirs()

			val file = File(folder, UUID.randomUUID().toString().split("-")[0] + ".wav")

			val value = args.drop(1).joinToString(" ").trim()

			event.message?.delete()?.complete()

			ThreadPool.run {
				try {
					if (this.generate(instance.voiceTextApiKey, value, file)) {
						VoicePlayer.play(instance, guild, member, channel, file)
					} else {
						file.delete()
						channel.sendMessage("音声を生成できませんでした。").complete()
					}
				} catch (e: Exception) {
					file.delete()
					e.printStackTrace()
				}
			}
			return
		}

		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription("使用方法: " + args.firstOrNull().orEmpty() + " <ボイス>")
		channel.sendMessageEmbeds(embedBuilder.build()).complete()
	}

	/**
	 * VoiceText Web API で text を読み上げた音声 (wav) を output に保存する。成功したら true を返す。
	 */
	private fun generate(apiKey: String, text: String, output: File): Boolean {
		val form = listOf("text" to text, "speaker" to "show", "pitch" to "150", "format" to "wav")
			.joinToString("&") { (key, value) ->
				URLEncoder.encode(key, Charsets.UTF_8) + "=" + URLEncoder.encode(value, Charsets.UTF_8)
			}

		// API キーをユーザー名、パスワードを空にした Basic 認証
		val authorization = "Basic " + Base64.getEncoder().encodeToString("$apiKey:".toByteArray(Charsets.UTF_8))

		val request = HttpRequest.newBuilder(URI.create(ENDPOINT))
			.timeout(Duration.ofSeconds(30))
			.header("Authorization", authorization)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.POST(HttpRequest.BodyPublishers.ofString(form))
			.build()

		val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
		response.body().use { body ->
			if (response.statusCode() != 200) {
				println("[VoiceText] 生成に失敗しました: HTTP " + response.statusCode())
				return false
			}

			Files.copy(body, output.toPath(), StandardCopyOption.REPLACE_EXISTING)
		}

		return true
	}

}
