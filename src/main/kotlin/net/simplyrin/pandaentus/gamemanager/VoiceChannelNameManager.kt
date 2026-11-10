package net.simplyrin.pandaentus.gamemanager

import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Member
import net.simplyrin.pandaentus.PandaEntus

/**
 * Created by SimplyRin on 2022/06/30.
 *
 * Copyright (c) 2022 SimplyRin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class VoiceChannelNameManager(val instance: PandaEntus) {

	val map = HashMap<String, List<String>>()

	// GuildId-MemberId -> Channel ID
	val joinedChannel = HashMap<String, Long>()

	fun updateVoiceChannelName(member: Member) {
		val voiceState = member.voiceState ?: return

		val channel = voiceState.channel ?: return

		for (m in channel.members) {
			for (a in m.activities) {
				this.updateVoiceChannelName(m, a)
			}
		}
	}

	fun updateVoiceChannelName(member: Member, activity: Activity?) {
		this.updateVoiceChannelName(member, activity?.name)
	}

	fun updateVoiceChannelName(member: Member, gameList: String?) {
		this.updateVoiceChannelName(member, if (gameList != null) listOf(gameList) else null)
	}

	fun updateVoiceChannelName(member: Member, lastGame: List<String>?) {
		println(member.effectiveName + ": Activity: " + lastGame)

		val channelId = this.joinedChannel[member.guild.id + "-" + member.id] ?: return

		val defaultName: String = this.instance.config.getString("DefaultChannelName.$channelId.Default", null) ?: return

		val channel = member.guild.getVoiceChannelById(channelId) ?: return

		val key = channel.id + "," + member.id

		if (lastGame != null) {
			this.map[key] = lastGame
		} else {
			this.map.remove(key)
		}

		// 更新
		val size = channel.members.size

		println(channel.id + ": " + this.map.values.toString())

		// ゲーム割合
		// ゲーム名 -> プレイ人数
		val games = HashMap<String, Int>()

		for (entry in this.map.entries) {
			if (entry.key.startsWith("$channelId,")) {
				for (lG in entry.value) {
					val count = (games[lG] ?: 0) + 1
					println("[Game] $lG: $count")
					games[lG] = count
				}
			}
		}

		println("$channelId($size): $defaultName")

		val sortList = this.sort(games)

		for (item in sortList) {
			println("VoiceChannelNameManager: " + item.key + ": " + item.value)
		}

		if (size > 0 && sortList.isNotEmpty()) {
			val item = sortList[0]

			println(item.key + ": " + item.value)

			if (item.value >= 2) {
				val name = (if (sortList.size == 1) "" else "？") + item.key
				if (channel.name != name) {
					channel.manager.setName(name).complete()
				}
				return
			}
		}

		if (channel.name != defaultName) {
			channel.manager.setName(defaultName).complete()
		}
	}

	fun sort(map: HashMap<String, Int>): List<Map.Entry<String, Int>> {
		return map.entries.sortedByDescending { it.value }
	}

}
