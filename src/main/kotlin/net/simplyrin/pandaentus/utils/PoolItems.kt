package net.simplyrin.pandaentus.utils

import java.io.File
import java.io.IOException

import net.simplyrin.config.Config
import net.simplyrin.config.Configuration
import net.simplyrin.pandaentus.PandaEntus

/**
 * Created by SimplyRin on 2019/09/16.
 *
 * Copyright (C) 2019 SimplyRin
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
class PoolItems @Suppress("UNUSED_PARAMETER") constructor(instance: PandaEntus) {

	private val file = File("pool.yml")
	private val config: Configuration

	init {
		if (!this.file.exists()) {
			try {
				this.file.createNewFile()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}

		this.config = Config.getConfig(this.file)
	}

	fun save() {
		Config.saveConfig(this.config, this.file)
	}

	fun setItem(key: String, value: String) {
		val trimmed = key.trim()

		println("Saved: '$trimmed', '$value'")
		this.config.set(trimmed.lowercase(), value)
		this.save()
	}

	fun getItem(key: String): String {
		val lowerKey = key.lowercase().trim()

		println("Detected key: '$lowerKey'")

		val item: String? = this.config.getString(lowerKey)

		println("Detected value: '$item'")

		if (item.isNullOrEmpty()) {
			println("Key: '$lowerKey' is not found! Return default value")
			return lowerKey
		}

		return item
	}

	fun getReaction(i: Int): String {
		return when (i) {
			1 -> "1⃣"
			2 -> "2⃣"
			3 -> "3⃣"
			4 -> "4⃣"
			5 -> "5⃣"
			6 -> "6⃣"
			else -> "🔄"
		}
	}

}
