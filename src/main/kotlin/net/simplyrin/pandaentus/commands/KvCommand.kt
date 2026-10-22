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
			val uniqueId = "./kv/" + UUID.randomUUID().toString().split("-")[0] + ".wav"
			File("kv").mkdirs()

			val file = File(uniqueId)

			val value = args.drop(1).joinToString(" ").trim()

			val command = arrayOf("curl", "https://api.voicetext.jp/v1/tts",
				"-o", uniqueId,
				"-u", instance.voiceTextApiKey + ":",
				"-d", "text=" + value,
				"-d", "speaker=show",
				"-d", "pitch=150",
				"-d", "format=wav")

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
