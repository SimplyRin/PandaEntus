package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import net.dv8tion.jda.api.EmbedBuilder
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.httpclient.HttpClient

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
class TextGenCommand : BaseCommand() {

	override val command = "!textgen"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		val embedBuilder = EmbedBuilder()

		if (args.size > 1) {
			val text = URLEncoder.encode(args[1], StandardCharsets.UTF_8)

			channel.sendTyping().complete()

			val httpClient = HttpClient("https://ja.cooltext.com/PostChange")
			httpClient.addHeader("origin", "https://ja.cooltext.com")
			httpClient.addHeader("accept-encoding", "gzip, deflate, br")
			httpClient.addHeader("accept-language", "ja-JP,ja;q=0.9,en;q=0.8,zh-CN;q=0.7,zh;q=0.6")
			httpClient.addHeader("user-agent", instance.botUserAgent)
			httpClient.addHeader("content-type", "application/x-www-form-urlencoded; charset=UTF-8")
			httpClient.addHeader("accept", "*/*")
			httpClient.addHeader("referer", "https://ja.cooltext.com/Logo-Design-Particle")
			httpClient.addHeader("authority", "ja.cooltext.com")
			httpClient.addHeader("x-requested-with", "XMLHttpRequest")

			httpClient.setData("LogoID=39&Text=$text&FontSize=70&Color1_color=23320000&Integer5=3&Integer7=0&Integer8=0&Integer14_color=#23000000^&Integer6=95&Integer9=0&Integer13=on&Integer12=on&FileFormat=6&BackgroundColor_color=#23FFFFFF")

			val result = httpClient.getAsJsonObject()
			println("Result: $result")

			val renderLocation = result?.get("renderLocation")?.asString
			if (renderLocation == null) {
				embedBuilder.setColor(Color.RED)
				embedBuilder.setDescription("画像の生成に失敗しました。")

				event.reply(embedBuilder.build())
				return
			}

			embedBuilder.setColor(Color.GREEN)
			embedBuilder.setDescription("Image generated!")
			embedBuilder.setImage(renderLocation)

			event.reply(embedBuilder.build())
			return
		}

		embedBuilder.setColor(Color.RED)
		embedBuilder.setDescription("使用方法: " + args.firstOrNull().orEmpty() + " <テキスト>")

		event.reply(embedBuilder.build())
	}

}
