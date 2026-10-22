package net.simplyrin.pandaentus.commands

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

/**
 * Created by SimplyRin on 2020/12/11.
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
class AmazonUrlCommand : BaseCommand() {

	override val command = "https://www.amazon.co.jp/"

	override val type = CommandType.StartsWith

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val url = args.firstOrNull() ?: return

		var result: String? = null

		for (a in listOf("/dp/", "/ASIN/", "/product/")) {
			if (url.contains(a)) {
				val id = url.split(a).getOrNull(1).orEmpty().substringBefore("/").substringBefore("?")

				result = "https://www.amazon.co.jp/dp/$id?tag=pndesports-22 (アフィリエイトリンク)"
				break
			}
		}

		if (result != null) {
			event.channel.sendMessage(result).complete()
		}
	}

}
