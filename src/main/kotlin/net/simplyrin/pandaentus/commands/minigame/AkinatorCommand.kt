package net.simplyrin.pandaentus.commands.minigame

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
class AkinatorCommand : BaseCommand() {

	override val command = "!akinator"

	override val description = "アキネーターに接続"

	override val commandData: CommandData
		get() = CommandDataImpl("akinator", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	// /akinator では開始ユーザーだけに見える返信をするため、チャンネルに入力中表示を出さない
	override fun sendTyping(): Boolean = false

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val slashCommandEvent = event.slashCommandEvent
		if (slashCommandEvent != null) {
			// 開始ユーザーだけに見えるメッセージで、全体プレイにするか自分だけで遊ぶかを聞く
			instance.akinatorManager.promptMode(slashCommandEvent)
			return
		}

		// !akinator はチャンネル全体でプレイする
		instance.akinatorManager.startPublic(event.channel, event.user, event.member.effectiveName)
	}

}
