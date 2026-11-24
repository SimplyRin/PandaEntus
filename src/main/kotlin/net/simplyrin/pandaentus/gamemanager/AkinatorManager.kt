package net.simplyrin.pandaentus.gamemanager

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.simplyrin.pandaentus.utils.ThreadPool

/**
 * Created by SimplyRin on 2020/03/18.
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

/**
 * 進行中のアキネーターをまとめて管理するリスナー (JDA に 1 つだけ登録する)
 *
 * - `!akinator`: チャンネル全体でプレイ (1 チャンネルにつき 1 ゲーム)
 * - `/akinator`: 開始ユーザーだけに見えるメッセージで、全体プレイにするか自分だけで遊ぶかを聞く。
 *   自分だけで遊んだ場合は、最後に結果をチャンネルへ共有するか聞く (1 ユーザーにつき 1 ゲーム)
 */
class AkinatorManager : ListenerAdapter() {

	/** セッション ID -> セッション */
	private val sessions = ConcurrentHashMap<String, AkinatorSession>()

	/** チャンネル ID -> 全体プレイのセッション ID */
	private val publicSessions = ConcurrentHashMap<String, String>()

	/** ユーザー ID -> 個人プレイのセッション ID */
	private val privateSessions = ConcurrentHashMap<String, String>()

	/** /akinator のモード選択待ち (トークン -> 作成時刻) */
	private val pendingModeChoices = ConcurrentHashMap<String, Long>()

	private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
		Thread(runnable, "Akinator-Timeout").apply { this.isDaemon = true }
	}

	init {
		this.scheduler.scheduleAtFixedRate({
			try {
				this.expireIdle()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}, 1, 1, TimeUnit.MINUTES)
	}

	/**
	 * `!akinator`: チャンネル全体で開始する
	 */
	fun startPublic(channel: MessageChannel, owner: User, ownerName: String) {
		val session = this.createPublicSession(channel, owner, ownerName)
		if (session == null) {
			channel.sendMessage("このチャンネルではすでにアキネーターが進行中です。").queue(null) { it.printStackTrace() }
			return
		}

		ThreadPool.run {
			try {
				channel.sendMessage(CONNECTING_MESSAGE).complete()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			session.start(null)
		}
	}

	/**
	 * `/akinator`: 開始ユーザーだけに見えるメッセージで、遊び方を選んでもらう
	 */
	fun promptMode(event: SlashCommandInteractionEvent) {
		val token = UUID.randomUUID().toString().substring(0, 8)
		this.pendingModeChoices[token] = System.currentTimeMillis()

		val content = "**アキネーターを始めます。どちらで遊びますか？**\n" +
			"・**自分だけで遊ぶ**: あなただけに見えるメッセージで進行します。最後に結果をチャンネルへ共有するか選べます\n" +
			"・**チャンネル全体で遊ぶ**: `!akinator` と同じく、このチャンネルの全員で回答します"

		event.reply(content)
			.setEphemeral(true)
			.setComponents(ActionRow.of(
				Button.primary(MODE_PREFIX + token + ":" + MODE_PRIVATE, "自分だけで遊ぶ"),
				Button.secondary(MODE_PREFIX + token + ":" + MODE_PUBLIC, "チャンネル全体で遊ぶ"),
			))
			.queue(null) { it.printStackTrace() }
	}

	override fun onButtonInteraction(event: ButtonInteractionEvent) {
		val componentId = event.componentId
		if (!componentId.startsWith(BUTTON_PREFIX)) {
			return
		}

		if (componentId.startsWith(MODE_PREFIX)) {
			this.onModeChosen(event, componentId.removePrefix(MODE_PREFIX))
			return
		}

		// aki:<セッション ID>:<質問番号>:<アクション>
		val parts = componentId.removePrefix(BUTTON_PREFIX).split(":")
		val session = parts.getOrNull(0)?.let { this.sessions[it] }
		val turn = parts.getOrNull(1)?.toIntOrNull()
		val action = parts.getOrNull(2)?.let { AkinatorSession.Action.fromCode(it) }

		if (session == null || turn == null || action == null) {
			event.reply("このアキネーターはすでに終了しています。").setEphemeral(true).queue()
			return
		}

		session.onButton(event, turn, action)
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (!event.isFromGuild || event.author.isBot) {
			return
		}

		val sessionId = this.publicSessions[event.channel.id] ?: return
		val session = this.sessions[sessionId] ?: return

		session.onText(event.author, event.message.contentRaw)
	}

	private fun onModeChosen(event: ButtonInteractionEvent, value: String) {
		val token = value.substringBefore(":")
		val mode = value.substringAfter(":")

		if (this.pendingModeChoices.remove(token) == null) {
			event.reply("この選択はすでに終了しています。もう一度 `/akinator` を実行してください。").setEphemeral(true).queue()
			return
		}

		val channel = event.channel
		val user = event.user
		val userName = event.member?.effectiveName ?: user.effectiveName

		when (mode) {
			MODE_PUBLIC -> {
				val session = this.createPublicSession(channel, user, userName)
				if (session == null) {
					event.editMessage("このチャンネルではすでにアキネーターが進行中です。").setComponents(emptyList()).queue()
					return
				}

				event.editMessage("チャンネル全体でアキネーターを開始しました。").setComponents(emptyList()).queue()
				ThreadPool.run {
					try {
						channel.sendMessage("**" + userName + " さんがアキネーターを開始しました！**\n" + CONNECTING_MESSAGE).complete()
					} catch (e: Exception) {
						e.printStackTrace()
					}
					session.start(null)
				}
			}
			MODE_PRIVATE -> {
				val session = AkinatorSession(this.newSessionId(), channel, user, userName, false, this::remove)

				// 同じユーザーの古い個人プレイは終了させる
				this.privateSessions.put(user.id, session.id)?.let { oldId ->
					this.sessions[oldId]?.terminate("新しいアキネーターを開始したため、このゲームは終了しました。")
				}
				this.sessions[session.id] = session

				event.editMessage(MessageEditBuilder()
					.setContent("**アキネーターに接続しています...。**")
					.setComponents(emptyList())
					.build()).queue()

				val hook = event.hook
				ThreadPool.run {
					session.start(hook)
				}
			}
			else -> event.reply("不明な選択です。").setEphemeral(true).queue()
		}
	}

	/**
	 * @return すでにそのチャンネルで全体プレイが進行中の場合は null
	 */
	private fun createPublicSession(channel: MessageChannel, owner: User, ownerName: String): AkinatorSession? {
		val session = AkinatorSession(this.newSessionId(), channel, owner, ownerName, true, this::remove)
		if (this.publicSessions.putIfAbsent(channel.id, session.id) != null) {
			return null
		}
		this.sessions[session.id] = session
		return session
	}

	private fun remove(session: AkinatorSession) {
		this.sessions.remove(session.id)
		if (session.isPublic) {
			this.publicSessions.remove(session.channel.id, session.id)
		} else {
			this.privateSessions.remove(session.owner.id, session.id)
		}
	}

	private fun expireIdle() {
		val now = System.currentTimeMillis()

		for (session in this.sessions.values) {
			if (session.state == AkinatorSession.State.CONNECTING) {
				continue
			}
			if (now - session.lastActivity >= IDLE_TIMEOUT_MILLIS) {
				session.terminate("⌛ 一定時間操作がなかったため、アキネーターを終了しました。")
			}
		}

		this.pendingModeChoices.entries.removeIf { now - it.value >= IDLE_TIMEOUT_MILLIS }
	}

	private fun newSessionId(): String = UUID.randomUUID().toString().substring(0, 8)

	companion object {

		const val BUTTON_PREFIX = "aki:"

		private const val MODE_PREFIX = BUTTON_PREFIX + "mode:"
		private const val MODE_PUBLIC = "public"
		private const val MODE_PRIVATE = "private"

		/**
		 * 個人プレイの ephemeral メッセージは InteractionHook (15 分で失効) でしか編集できないため、それより短くする
		 */
		private val IDLE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10)

		private const val CONNECTING_MESSAGE = "アキネーターに接続しています...。\n" +
			"ボタン、または番号 (1〜5) を発言して回答してください。一つ戻る場合は \"もどる\" (7)、中止する場合は \"やめる\" (8)。"

	}

}
