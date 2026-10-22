package net.simplyrin.pandaentus.commands

import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Date

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

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
class ProfileCommand : BaseCommand() {

	override val command = "!profile"

	override val description = "あなたのアカウント作成日を表示"

	override val commandData: CommandData
		get() = CommandDataImpl("profile", this.description)
			.addOption(OptionType.USER, "ユーザー", "指定したユーザーのアカウント作成日を確認", false)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setColor(Color.ORANGE)

		val arguments = if (event.isSlashCommand()) {
			arrayOf(this.command, event.slashCommandEvent?.getOption("ユーザー")?.asString ?: event.user.id)
		} else {
			args
		}

		var member = event.member
		if (arguments.size > 1) {
			val id = arguments[1].replace("<", "").replace(">", "").replace("@", "").replace("!", "")

			// 数値以外の ID など、取得できなかった場合は実行者自身の情報を表示する
			member = try {
				event.guild.getMemberById(id)
			} catch (e: Exception) {
				null
			} ?: member
		}

		val date = Date(member.user.timeCreated.toInstant().toEpochMilli())
		val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

		embedBuilder.addField("アカウント名", member.effectiveName, false)
		embedBuilder.addField("アカウント作成日", simpleDateFormat.format(date), false)

		event.reply(embedBuilder.build())
	}

}
