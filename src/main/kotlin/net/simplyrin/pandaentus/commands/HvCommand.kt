package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.io.File
import java.util.UUID

import net.dv8tion.jda.api.EmbedBuilder
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.coefont.CoeFontClient
import net.simplyrin.pandaentus.coefont.CoeFontLimiter
import net.simplyrin.pandaentus.tts.TtsManager
import net.simplyrin.pandaentus.utils.ThreadPool

/**
 * Created by SimplyRin on 2023/03/30.
 *
 * Copyright (c) 2023 SimplyRin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
/**
 * CoeFont CLOUD の「ひろゆき」の声で、入力した文章を通話で再生する。1 日 5 回まで (Bot 全体)。
 */
class HvCommand : BaseCommand() {

	override val command = "!hv"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel
		val member = event.member
		val guild = event.guild

		val voiceChannel = member.voiceState?.channel
		if (voiceChannel == null) {
			this.error(event, "ボイスチャンネルに接続してください！")
			return
		}

		if (args.size <= 1) {
			this.error(event, "使用方法: " + args.firstOrNull().orEmpty() + " <ボイス> (" + CoeFontClient.MAX_LENGTH + " 文字まで、1 日 " + CoeFontLimiter.DAILY_LIMIT + " 回まで)")
			return
		}

		val text = args.drop(1).joinToString(" ").trim()
		if (text.length > CoeFontClient.MAX_LENGTH) {
			this.error(event, CoeFontClient.MAX_LENGTH.toString() + " 文字までです。(" + text.length + " 文字)")
			return
		}

		val connectedChannel = guild.audioManager.connectedChannel
		if (connectedChannel != null && connectedChannel.idLong != voiceChannel.idLong) {
			this.error(event, "Bot は別のボイスチャンネル (" + connectedChannel.name + ") に参加しています。")
			return
		}

		val limiter = instance.coeFontLimiter
		if (!limiter.tryAcquire()) {
			this.error(event, "今日の使用回数 (" + CoeFontLimiter.DAILY_LIMIT + " 回) に達しました。日本時間の 0 時にリセットされます。")
			return
		}

		val message = event.reply("🎤 音声を生成しています... (今日の残り " + limiter.remaining() + " 回)")

		ThreadPool.run {
			TtsManager.FOLDER.mkdirs()
			val file = File(TtsManager.FOLDER, "hv-" + UUID.randomUUID().toString() + ".wav")

			try {
				instance.coeFontClient.generate(text, file)
			} catch (e: CoeFontClient.Companion.GenerateException) {
				// 生成できなかったときは、使用回数を戻す
				file.delete()
				limiter.release()
				println("[CoeFont] " + e.message + (e.cause?.let { " (" + it + ")" } ?: ""))
				message.editMessage("❌ " + e.message).queue()
				return@run
			}

			instance.ttsManager.connect(guild, voiceChannel)
			instance.ttsManager.enqueueFile(guild, file)

			message.editMessage("🎤 再生します。(今日の残り " + limiter.remaining() + " 回)").queue()
		}
	}

	private fun error(event: PandaMessageEvent, description: String) {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription(description)
		event.channel.sendMessageEmbeds(embedBuilder.build()).complete()
	}

}
