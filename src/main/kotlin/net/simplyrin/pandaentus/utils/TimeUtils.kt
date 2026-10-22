package net.simplyrin.pandaentus.utils

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by SimplyRin on 2020/05/20.
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
class TimeUtils {

	private val callTime = ConcurrentHashMap<String, CallTime>()

	fun get(guildId: String, userId: String): CallTime {
		val key = "$guildId,$userId"
		return this.callTime.computeIfAbsent(key) { CallTime(key, userId) }
	}

	fun resetGuild(guildId: String) {
		for (callTime in this.getList()) {
			if (callTime.key.startsWith("$guildId,")) {
				this.callTime.remove(callTime.key)
			}
		}
	}

	fun getList(guildId: String): List<CallTime> {
		val list = mutableListOf<CallTime>()
		for (callTime in this.getList()) {
			println("Checking ${callTime.key}, ${callTime.totalTime}")
			if (callTime.key.startsWith("$guildId,")) {
				println("[ADDED] - Checking ${callTime.key}, ${callTime.totalTime}")
				list.add(callTime)
			}
		}
		return list
	}

	fun getList(): List<CallTime> {
		return this.callTime.values.toList()
	}

	inner class CallTime(internal val key: String, val name: String) {

		internal var totalTime = 0
		private var joined = 0L

		private var alreadyJoined = false

		fun join() {
			if (this.alreadyJoined) {
				return
			}

			this.alreadyJoined = true
			this.joined = System.currentTimeMillis()
		}

		fun quit() {
			this.alreadyJoined = false

			if (this.joined == 0L) {
				return
			}
			val end = (System.currentTimeMillis() - this.joined) / 1000
			this.joined = 0

			this.totalTime += end.toInt()
		}

		fun getTime(): String {
			var print = ""

			val hour = this.totalTime / 3600
			if (hour >= 1) {
				print += "${hour}時間 "
			}

			val minute = this.totalTime % 3600 / 60
			if (minute >= 1) {
				print += "${minute}分 "
			} else if (hour >= 1) {
				print += "0分 "
			}

			val seconds = this.totalTime % 3600 % 60
			if (seconds >= 1) {
				print += "${seconds}秒"
			} else if (hour >= 1 || minute >= 1) {
				print += "0秒"
			}

			if (print.isEmpty()) {
				print = "0秒"
			}

			return print
		}

	}

}
