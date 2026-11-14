package net.simplyrin.pandaentus.commands.botowner

import java.awt.Color
import java.math.BigDecimal
import java.math.MathContext

import net.dv8tion.jda.api.EmbedBuilder
import net.objecthunter.exp4j.ExpressionBuilder
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
class CalcCommand : BaseCommand() {

	companion object {

		// 計算式の長さの上限
		private const val MAX_LENGTH = 200

		/**
		 * 計算式を計算して、結果を文字列で返す。計算できなければ null を返す。
		 *
		 * 計算は Java のライブラリ (exp4j) の中だけで行い、外部コマンドは実行しない。
		 * 使えるのは、数値、四則演算、べき乗、括弧と、exp4j の組み込み関数 (sin, sqrt など) だけ。
		 */
		@JvmStatic
		fun calculate(input: String): String? {
			if (input.isBlank() || input.length > MAX_LENGTH) {
				return null
			}

			val result = try {
				ExpressionBuilder(input).build().evaluate()
			} catch (e: Exception) {
				// 式として読み取れない、0 で割った、など
				return null
			}

			if (result.isNaN() || result.isInfinite()) {
				return null
			}

			// 0.1+0.2 が 0.30000000000000004 にならないように、15 桁に丸めてから表示する
			return BigDecimal(result).round(MathContext(15)).stripTrailingZeros().toPlainString()
		}

	}

	override val command = "="

	override val type = CommandType.StartsWith

	override val permission = CommandPermission.BotOwner

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val channel = event.channel

		// "=1 + 1" のように空白を入れた式も、1 つの式として扱う
		val input = args.joinToString("").replace("=", "")
		if (input.isEmpty()) {
			val embedBuilder = EmbedBuilder()
			embedBuilder.setColor(Color.RED)
			embedBuilder.setDescription("使用方法: =<計算式>\n=1+1")
			channel.sendMessageEmbeds(embedBuilder.build()).complete()
			return
		}

		val result = calculate(input)
		if (result == null) {
			channel.sendMessage("計算できませんでした。").complete()
			return
		}

		channel.sendMessage("結果: **$result**").complete()
	}

}
