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
import net.simplyrin.pandaentus.utils.VoicePlayer
import net.simplyrin.processmanager.Callback
import net.simplyrin.processmanager.ProcessManager

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
class HvCommand : BaseCommand() {

	override val command = "!hv"

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
			val uniqueId = "./hv/" + UUID.randomUUID().toString().split("-")[0] + ".wav"
			File("hv").mkdirs()

			val file = File(uniqueId)

			val value = args.drop(1).joinToString(" ").trim()

			val command = arrayOf("tarakotalk", "save", "\"" + value + "\"", uniqueId)

			event.message?.delete()?.complete()

			ProcessManager.runCommand(command, object : Callback() {

				override fun line(response: String?) {
					println(response)
				}

				override fun processEnded() {
					try {
						Thread.sleep(500)
					} catch (e: Exception) {
					}

					VoicePlayer.play(instance, guild, member, channel, file)
				}

			}, true)
			return
		}

		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription("使用方法: " + args.firstOrNull().orEmpty() + " <ボイス>")
		channel.sendMessageEmbeds(embedBuilder.build()).complete()
	}

}
