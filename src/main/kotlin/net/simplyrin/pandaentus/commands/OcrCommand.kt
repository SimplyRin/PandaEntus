package net.simplyrin.pandaentus.commands

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import javax.net.ssl.HttpsURLConnection

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.sourceforge.tess4j.Tesseract

/**
 * Created by SimplyRin on 2021/11/30.
 *
 * Copyright (C) 2021 SimplyRin
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
class OcrCommand : BaseCommand() {

	override val command = "!ocr"

	override val description = "画像から文字を解析"

	override val commandData: CommandData
		get() = CommandDataImpl("ocr", this.description)
			.addOption(OptionType.STRING, "言語", "jpn, eng")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val requestedLanguage = if (event.isSlashCommand()) {
			event.slashCommandEvent?.getOption("言語")?.asString
		} else {
			args.getOrNull(1)
		}
		val language = requestedLanguage?.lowercase()?.takeIf { it == "jpn" || it == "eng" } ?: "jpn"

		val attachments = event.message?.attachments.orEmpty()

		if (attachments.isEmpty()) {
			event.reply("画像ファイルを付けてもう一度実行してください。")
			return
		}

		for (attachment in attachments) {
			val extension = attachment.fileExtension?.lowercase()
			if (extension == null || !(extension == "jpg" || extension == "png" || extension == "jpeg")) {
				event.reply(".JPG, .PNG のみ対応しています。")
				continue
			}

			val ocr = File("ocr")
			ocr.mkdirs()

			val file = File(ocr, UUID.randomUUID().toString().split("-")[0] + "." + extension)

			try {
				val connection = URI(attachment.url).toURL().openConnection() as HttpsURLConnection
				connection.addRequestProperty("user-agent", instance.botUserAgent)

				connection.inputStream.use { inputStream ->
					Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
				}

				val tess = Tesseract()
				tess.setDatapath(File("tessdata").absolutePath)
				tess.setLanguage(language)

				val value = tess.doOCR(file)

				event.reply("```$value```")
			} catch (e: Exception) {
				event.reply("解析に失敗しました。")
			} finally {
				file.delete()
			}
		}
	}

}
