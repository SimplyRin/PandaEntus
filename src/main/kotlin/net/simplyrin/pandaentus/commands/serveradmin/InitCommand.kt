package net.simplyrin.pandaentus.commands.serveradmin

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
class InitCommand : BaseCommand() {

	override val command = "!init"

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.ServerAdministrator

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel
		val guild = event.guild

		if (args.size > 1 && args[1].equals("confirm", ignoreCase = true)) {
			val textCategory = guild.createCategory("Text Channels").complete()
			val textChannel = textCategory.createTextChannel("general-1").complete()

			val voiceCategory = guild.createCategory("Voice Channels").complete()
			voiceCategory.createVoiceChannel("General-1").setUserlimit(99).complete()

			textChannel.sendMessage("Bot で必要なカテゴリ、チャンネルを自動作成しました。").complete()
			return
		}

		channel.sendMessage("!init confirm と入力すると、Bot で必要なカテゴリ、チャンネルを自動的に作成します。\n" +
			"作成されるチャンネルは以下の通りです。\nカテゴリ: `Text Channels`, `Voice Channels`" +
			"\nテキストチャンネル: `#general`" +
			"\nボイスチャンネル: `General-1`").complete()
	}

}
