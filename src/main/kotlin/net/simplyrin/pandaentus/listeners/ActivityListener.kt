package net.simplyrin.pandaentus.listeners

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
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

	companion object {

		// YAML のパス区切りと衝突しないよう、ゲーム名の "." を置き換える文字 (ONE DOT LEADER)
		private const val DOT_REPLACEMENT = '\u2024'

		/**
		 * ゲーム名を activity.yml のキーとして使える形に変換する
		 */
		fun encodeGameKey(name: String): String {
			return name.replace('.', DOT_REPLACEMENT)
		}

		/**
		 * activity.yml のキーをゲーム名に戻す
		 */
		fun decodeGameKey(key: String): String {
			return key.replace(DOT_REPLACEMENT, '.')
		}

		/**
		 * プレイ時間の記録対象にするアクティビティ (ゲームのみ。Spotify やカスタムステータスなどは対象外)
		 */
		private fun isGame(activity: Activity): Boolean {
			return activity.type == Activity.ActivityType.PLAYING
		}

	}

	// イベントスレッドと監視スレッドから同時にアクセスされるため並行安全なコレクションを使う
	// GuildId-UserId -> プレイ中のゲーム名
	val map: MutableMap<String, MutableList<String>> = ConcurrentHashMap()

	init {
		// 起動前からプレイ中のユーザーを拾う
		for (guild in this.instance.jda.guilds) {
			this.syncGuild(guild)
		}

		this.startManager()
	}

	fun startManager() {
		Thread {
			while (true) {
				try {
					this.recordPlayedMinutes()
				} catch (e: Exception) {
					// 例外で監視スレッドが止まらないようにする
					e.printStackTrace()
				}

				try {
					TimeUnit.MINUTES.sleep(1)
				} catch (e: Exception) {
				}
			}
		}.start()
	}

	/**
	 * プレイ中のゲームの記録を 1 分進める
	 */
	private fun recordPlayedMinutes() {
		var changed = false

		for ((id, games) in this.map.entries) {
			// GuildId-UserId
			val (guildId, userId) = id.split("-", limit = 2)

			// 終了イベントを取りこぼしても加算され続けないよう、現在のプレゼンスと照合する
			val member = this.instance.jda.getGuildById(guildId)?.getMemberById(userId)
			if (member == null) {
				this.map.remove(id)
				continue
			}

			val playing = member.activities.filter { isGame(it) }.map { it.name }.distinct()
			games.retainAll(playing.toSet())
			for (game in playing) {
				if (!games.contains(game)) {
					games.add(game)
				}
			}

			val enabled = this.instance.config.getBoolean("Guild.$guildId.$userId.IsEnabledActivity", false)
			if (!enabled) {
				continue
			}

			synchronized(this.instance.activityConfig) {
				for (game in games) {
					val configKey = "Guild.$guildId.$userId.Game." + encodeGameKey(game)

					val playedMinutes = this.instance.activityConfig.getInt(configKey, 0)

					this.instance.activityConfig.set(configKey, playedMinutes + 1)
					changed = true
				}
			}
		}

		if (changed) {
			this.instance.saveActivityConfig()
		}
	}

	/**
	 * サーバー内のメンバーが現在プレイ中のゲームを取り込む
	 */
	private fun syncGuild(guild: Guild) {
		for (member in guild.members) {
			if (member.user.isBot) {
				continue
			}

			for (activity in member.activities) {
				if (isGame(activity)) {
					this.addGame(guild.id + "-" + member.id, activity.name)
				}
			}
		}
	}

	private fun addGame(id: String, game: String) {
		val games = this.map.computeIfAbsent(id) { CopyOnWriteArrayList() }
		if (!games.contains(game)) {
			games.add(game)
		}
	}

	override fun onGuildReady(event: GuildReadyEvent) {
		this.syncGuild(event.guild)
	}

	override fun onUserActivityStart(event: UserActivityStartEvent) {
		val guild = event.guild
		val member = event.member
		val activity = event.newActivity

		if (member.user.isBot || !isGame(activity)) {
			return
		}

		// this.instance.vcNameManager.updateVoiceChannelName(member, activity)

		this.addGame(guild.id + "-" + member.id, activity.name)

		println("[ActivityListener-START] " + member.effectiveName + "@" + guild.id + ": " + activity.name)
	}

	override fun onUserActivityEnd(event: UserActivityEndEvent) {
		val guild = event.guild
		val member = event.member
		val activity = event.oldActivity

		if (member.user.isBot || !isGame(activity)) {
			return
		}

		// this.instance.vcNameManager.updateVoiceChannelName(member)

		this.map[guild.id + "-" + member.id]?.remove(activity.name)

		println("[ActivityListener-END] " + member.effectiveName + "@" + guild.id + ": " + activity.name)
	}

}
