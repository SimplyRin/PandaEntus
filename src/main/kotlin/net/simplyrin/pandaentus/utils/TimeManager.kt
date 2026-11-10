package net.simplyrin.pandaentus.utils

import java.util.Date
import java.util.concurrent.ConcurrentHashMap

import net.simplyrin.pandaentus.PandaEntus

/**
 * Created by SimplyRin on 2019/04/04.
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
class TimeManager @JvmOverloads constructor(private val instance: PandaEntus, private val userId: String? = null) {

	companion object {
		private val maps = ConcurrentHashMap<String, TimeManager>()
	}

	fun getUser(userId: String): TimeManager {
		return maps.computeIfAbsent(userId) { TimeManager(this.instance, userId) }
	}

	private var date: Date? = null

	var isJoined = false
		private set

	fun joined() {
		this.date = Date()
		this.isJoined = true
	}

	fun quit() {
		this.date = null
		this.isJoined = false
		this.userId?.let { maps.remove(it) }
	}

	@Suppress("DEPRECATION")
	fun getJoinedTime(): String {
		val date = this.date ?: return ""
		return "${date.hours}時${date.minutes}分"
	}

	fun getCurrentTime(): String {
		return this.instance.getUptime(this.date ?: Date())
	}

}
