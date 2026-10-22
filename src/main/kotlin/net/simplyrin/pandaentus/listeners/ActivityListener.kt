package net.simplyrin.pandaentus.listeners

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

import net.dv8tion.jda.api.events.user.UserActivityEndEvent
import net.dv8tion.jda.api.events.user.UserActivityStartEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.simplyrin.pandaentus.PandaEntus

/**
 * Created by SimplyRin on 2022/04/03.
 *
 * Copyright (C) 2022 SimplyRin
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
class ActivityListener(val instance: PandaEntus) : ListenerAdapter() {

	// イベントスレッドと監視スレッドから同時にアクセスされるため並行安全なコレクションを使う
	val map: MutableMap<String, MutableList<String>> = ConcurrentHashMap()

	init {
		this.startManager()
	}

	fun startManager() {
		Thread {
			while (true) {
				for (entry in this.map.entries) {
					// GuildId-UserId
					val id = entry.key

					val guildId = id.split("-")[0]
					val userId = id.split("-")[1]

					val enabled = this.instance.config.getBoolean("Guild.$guildId.$userId.IsEnabledActivity", false)
					if (!enabled) {
						continue
					}

					val gameList = entry.value

					for (game in gameList) {
						val configKey = "Guild.$guildId.$userId.Game.$game"

						var playedMinutes = this.instance.activityConfig.getInt(configKey, 0)

						playedMinutes++

						this.instance.activityConfig.set(configKey, playedMinutes)
					}
				}

				this.instance.saveActivityConfig()

				try {
					TimeUnit.MINUTES.sleep(1)
				} catch (e: Exception) {
				}
			}
		}.start()
	}

	override fun onUserActivityStart(event: UserActivityStartEvent) {
		val guild = event.guild
		val member = event.member
		val activity = event.newActivity

		if (member.user.isBot) {
			return
		}

		// this.instance.vcNameManager.updateVoiceChannelName(member, activity)

		val games = this.map.computeIfAbsent(guild.id + "-" + member.id) { CopyOnWriteArrayList() }
		if (!games.contains(activity.name)) {
			games.add(activity.name)
		}

		println("[ActivityListener-START] " + member.effectiveName + "@" + guild.id + ": " + activity.name)
	}

	override fun onUserActivityEnd(event: UserActivityEndEvent) {
		val guild = event.guild
		val member = event.member

		if (member.user.isBot) {
			return
		}

		// this.instance.vcNameManager.updateVoiceChannelName(member)

		val activity = event.oldActivity

		val games = this.map.computeIfAbsent(guild.id + "-" + member.id) { CopyOnWriteArrayList() }
		games.remove(activity.name)

		println("[ActivityListener-END] " + member.effectiveName + "@" + guild.id + ": " + activity.name)
	}

}
