package net.simplyrin.pandaentus.gamemanager.wordwolf

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.commands.minigame.WordWolfCommand
import net.simplyrin.pandaentus.utils.ThreadPool

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
class WordWolfManager(
	val instance: PandaEntus,
	val guild: Guild,
	val channel: MessageChannel,
	val wolfs: Int,
	seconds: Int,
	theme: String?
) : ListenerAdapter() {

	@Volatile
	var seconds: Int = seconds
		private set

	val gameId: String = UUID.randomUUID().toString().split("-")[0]

	var gameStarted = false
		private set
	@Volatile
	var isGameEnd = false
		private set

	// 参加者の Discord ID
	val players: MutableList<String> = ArrayList()

	// リアクションメッセージ
	var recruitMessage: Message? = null
		private set

	// 多数派お題
	val playerTheme: String
	// 少数派お題
	val wolfTheme: String

	// 市民
	val generalPlayers: MutableList<String> = ArrayList()
	// 狼
	val wolfPlayers: MutableList<String> = ArrayList()

	// 投票用
	val voteMap = HashMap<Int, Member>()
	// 投票受付状態
	var voteReception = false
		private set
	// 投票されたマップ
	val votedMap = HashMap<Member, Int>()
	// 投票数
	var votedCount = 0
		private set
	// 投票が必要なユーザー
	val voteNeededPlayers: MutableList<String> = ArrayList()

	init {
		// テーマファイル特定
		val wwt = if (theme == null) WordWolfTheme.getTheme() else WordWolfTheme.getTheme(theme)
		this.playerTheme = wwt.a
		this.wolfTheme = wwt.b

		this.instance.jda.addEventListener(this)

		games[this.gameId] = this
	}

	fun startRecruit(event: PandaMessageEvent): WordWolfManager {
		val recruitMessage = event.reply("ワードウルフのプレイヤー募集をします。\n"
			+ "参加したいユーザーはリアクション 👌 を押して待機してください。 (" + this.gameId + ")")
		recruitMessage.addReaction(Emoji.fromFormatted("👌")).complete()
		this.recruitMessage = recruitMessage

		return this
	}

	fun start(): WordWolfManager? {
		if (this.gameStarted) {
			return null
		}

		if (this.players.size <= 1) {
			return this
		}

		this.gameStarted = true

		// ウルフ
		val ids = this.players.toMutableList()
		ids.shuffle()

		var wolf = this.wolfs
		while (wolf >= 1) {
			wolf--

			this.wolfPlayers.add(ids.removeAt(0))
		}
		this.generalPlayers.addAll(ids)

		for (wolfP in this.wolfPlayers) {
			val member = this.guild.getMemberById(wolfP) ?: continue
			member.user.openPrivateChannel().complete().sendMessage("ゲームが始まりました。お題: **" + this.wolfTheme + "**").complete()
		}

		for (generalP in this.generalPlayers) {
			val member = this.guild.getMemberById(generalP) ?: continue
			member.user.openPrivateChannel().complete().sendMessage("ゲームが始まりました。お題: **" + this.playerTheme + "**").complete()
		}

		this.channel.sendMessage("個人チャットへお題を送信しました。\nゲームを開始しました。お題に従って話し合いを始めてください。").complete()

		val message = this.channel.sendMessage("残り時間: " + this.instance.formatMillis(this.seconds * 1000L)).complete()

		ThreadPool.run {
			while (this.seconds >= 0) {
				try {
					TimeUnit.SECONDS.sleep(1)
				} catch (e: Exception) {
				}
				this.seconds--
			}

			// ゲーム終了
			this.isGameEnd = true
		}

		ThreadPool.run {
			while (true) {
				try {
					TimeUnit.SECONDS.sleep(4)
				} catch (e: InterruptedException) {
					e.printStackTrace()
				}

				if (this.isGameEnd) {
					message.editMessage("残り時間: 0:00").complete()
					var mention = ""
					for (id in this.players) {
						val mem = this.guild.getMemberById(id) ?: continue
						mention += mem.asMention + " "
					}
					this.channel.sendMessage(mention + "\n話し合いの時間が終了しました。\n個人チャットへ送信したメッセージに従ってください。").complete()
					this.voteReception = true
					break
				} else {
					ThreadPool.run { message.editMessage("残り時間: " + this.instance.formatMillis(this.seconds * 1000L)).complete() }
				}
			}

			var members = ""

			var i = 1
			for (id in this.players) {
				val member = this.guild.getMemberById(id) ?: continue
				this.voteMap[i] = member

				members += i.toString() + ": " + member.effectiveName + "\n"
				i++

				this.voteNeededPlayers.add(id)
			}

			for (id in this.players) {
				val member = this.guild.getMemberById(id) ?: continue
				member.user.openPrivateChannel().complete().sendMessage("狼だと思う人の名前または、最初の番号を入力してください。\n```$members```").complete()
			}

			ThreadPool.run {
				try {
					TimeUnit.SECONDS.sleep(60)
				} catch (e: Exception) {
				}

				// TODO まだ投票が終了していない場合強制的にゲームを終了する
				if (this.voteNeededPlayers.size >= 1) {
					this.channel.sendMessage("下記のプレイヤーが投票を行いませんでしたが、時間制限のためゲームを終了しました。").complete()

					this.showResult()
				}
			}
		}

		return this
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.channelType != ChannelType.PRIVATE) {
			return
		}

		if (!this.voteReception) {
			return
		}

		if (event.author.isBot) {
			return
		}

		if (!this.voteNeededPlayers.contains(event.author.id)) {
			event.channel.sendMessage("あなたは既に投票済みです！").complete()
			return
		}

		val message = event.message.contentRaw
		val msgId = message.toIntOrNull() ?: 999

		// 投票ユーザー
		var member: Member? = null

		for ((id, m) in this.voteMap) {
			if (id == msgId) {
				member = m
			}

			if (m.effectiveName.equals(message, ignoreCase = true)) {
				member = m
			}
		}

		if (member != null) {
			this.votedMap[member] = (this.votedMap[member] ?: 0) + 1
			this.votedCount++
			event.channel.sendMessage(member.effectiveName + " に投票しました。").complete()
			this.voteNeededPlayers.remove(event.author.id)

			// 投票終了
			if (this.votedCount == this.players.size) {
				this.showResult()
			}
		}
	}

	fun showResult() {
		this.voteNeededPlayers.clear()

		var msg = ""

		msg += "人狼: " + this.wolfTheme + "\n"
		for (id in this.wolfPlayers) {
			val member = this.guild.getMemberById(id) ?: continue
			msg += member.effectiveName + " (投票数: " + this.getVotedCount(member) + ")\n"
		}

		msg += "\n市民: " + this.playerTheme + "\n"
		for (id in this.generalPlayers) {
			val member = this.guild.getMemberById(id) ?: continue
			msg += member.effectiveName + " (投票数: " + this.getVotedCount(member) + ")\n"
		}

		val commandName = this.instance.commandRegister.getRegisteredCommand(WordWolfCommand::class.java)?.command.orEmpty()
		this.channel.sendMessage("ゲームが終了しました。結果:\n```$msg```\nもう一度始める場合は **$commandName** を使用してください。").complete()

		this.voteReception = false

		this.instance.jda.removeEventListener(this)
		games.remove(this.gameId)
	}

	fun getVotedCount(member: Member): Int {
		var count = 0
		for ((key, value) in this.votedMap) {
			if (key.id == member.id) {
				count = value
			}
		}
		return count
	}

	override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
		if (event.user?.isBot == true) {
			return
		}

		// 募集メッセージ以外へのリアクション、開始後、参加済みのユーザーは無視する
		val recruitMessage = this.recruitMessage
		if (recruitMessage == null || recruitMessage.id != event.messageId) {
			return
		}
		if (this.gameStarted || this.players.contains(event.userId)) {
			return
		}

		val commandName = this.instance.commandRegister.getRegisteredCommand(WordWolfCommand::class.java)?.command.orEmpty()

		val member = event.member ?: return

		this.players.add(member.id)

		val channel = member.user.openPrivateChannel().complete()
		channel.sendMessage("ワードウルフへの参加を受け付けました。\n"
			+ "ゲーム開始までお待ちください。\n"
			+ "開始コマンド: **" + commandName + " start**").complete()
	}

	companion object {

		private val games = ConcurrentHashMap<String, WordWolfManager>()

		@JvmStatic
		fun getGame(gameId: String): WordWolfManager? {
			return games[gameId]
		}

		@JvmStatic
		fun getGameByChannel(channel: MessageChannel): WordWolfManager? {
			var wwm: WordWolfManager? = null
			for (value in games.values) {
				if (value.channel.id == channel.id) {
					wwm = value
				}
			}
			return wwm
		}

	}

}
