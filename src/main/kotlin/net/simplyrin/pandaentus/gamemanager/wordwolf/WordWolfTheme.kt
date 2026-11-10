package net.simplyrin.pandaentus.gamemanager.wordwolf

import java.io.File

import net.simplyrin.config.Config

/**
 * Created by SimplyRin on 2021/11/09.
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
class WordWolfTheme private constructor(val baseTheme: String, val a: String, val b: String) {

	companion object {

		private val folder = File("wordwolf")

		@JvmStatic
		fun getTheme(name: String): WordWolfTheme {
			var file: File? = null
			for (value in folder.listFiles().orEmpty()) {
				val config = Config.getConfig(value)
				if (name.equals(config.getString("name"), ignoreCase = true)) {
					file = value
				}
			}
			return getTheme(requireNotNull(file) { "テーマが見つかりません: $name" })
		}

		/**
		 * 保存されているファイル全てからテーマをランダムに取得
		 */
		@JvmStatic
		fun getTheme(): WordWolfTheme {
			val file = folder.listFiles().orEmpty().toList().shuffled().firstOrNull()
			return getTheme(requireNotNull(file) { "テーマファイルが見つかりません" })
		}

		/**
		 * 指定したファイルからテーマをランダムに取得
		 */
		@JvmStatic
		fun getTheme(file: File): WordWolfTheme {
			val config = Config.getConfig(file)

			val themes = config.getStringList("themes").shuffled()

			val st = themes.firstOrNull()?.split("|")
			val a = st?.getOrNull(0)
			val b = st?.getOrNull(1)
			requireNotNull(a) { "テーマの定義が不正です: " + file.name }
			requireNotNull(b) { "テーマの定義が不正です: " + file.name }

			return WordWolfTheme(config.getString("name"), a, b)
		}

	}

}
