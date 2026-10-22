package net.simplyrin.pandaentus.gamemanager

import java.util.concurrent.TimeUnit

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.simplyrin.pandaentus.utils.ThreadPool
import org.eu.zajc.akiwrapper.Akiwrapper
import org.eu.zajc.akiwrapper.AkiwrapperBuilder
import org.eu.zajc.akiwrapper.core.entities.Guess
import org.eu.zajc.akiwrapper.core.entities.Query
import org.eu.zajc.akiwrapper.core.entities.Question

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
class AkinatorManager(val guild: Guild, val channelId: String) : ListenerAdapter() {

	var akiWrapper: Akiwrapper? = AkiwrapperBuilder()
		.setFilterProfanity(false)
		.setLanguage(Akiwrapper.Language.JAPANESE)
		.build()
		private set

	var latestMessageId: String? = null

	var lastChannel: String? = null
		private set

	@Volatile
	var isGameEnd = false
		private set

	var query: Query? = null
		private set

	// 他スレッドの onMessageReceived から更新されるため volatile にする
	@Volatile
	var isReceivingQuestion = false
		private set
	var question: Question? = null
		private set

	var guess: Guess? = null
		private set

	@Volatile
	private var checkEnd = false

	init {
		this.guild.jda.addEventListener(this)

		this.runGame()
	}

	/**
	 * ゲームが終了するまでブロックして質問を送り続ける
	 */
	private fun runGame() {
		val channel = this.guild.getTextChannelById(this.channelId) ?: return

		this.query = this.akiWrapper?.currentQuery

		while (true) {
			val currentQuery = this.query ?: break

			if (this.isGameEnd) {
				break
			}

			var embedBuilder = EmbedBuilder()

			if (currentQuery is Question) {
				// onMessageReceived を有効にして、結果を送る

				this.isReceivingQuestion = true
				this.question = currentQuery

				embedBuilder = this.setEmbed(currentQuery)

				channel.sendMessageEmbeds(embedBuilder.build()).complete()

				while (this.isReceivingQuestion) {
					try {
						TimeUnit.SECONDS.sleep(1)
					} catch (e: InterruptedException) {
						throw RuntimeException(e)
					}
				}
			} else if (currentQuery is Guess) {
				this.guess = currentQuery

				embedBuilder.setAuthor("あなたが想像しているのは...")
				embedBuilder.setDescription(currentQuery.name + " (" + currentQuery.description + ")")
				embedBuilder.setImage(currentQuery.image?.toExternalForm())
				embedBuilder.addField("正解！", "1", true)
				embedBuilder.addField("違うので続ける...", "2", true)

				this.isReceivingQuestion = true
				this.checkEnd = true
				channel.sendMessageEmbeds(embedBuilder.build()).complete()

				if (currentQuery.description != "---") {
					println(currentQuery.description)
				}

				while (this.checkEnd) {
					try {
						TimeUnit.SECONDS.sleep(1)
					} catch (e: InterruptedException) {
						throw RuntimeException(e)
					}
				}
			}
		}

		println("Bravo!")
	}

	fun parseAnswer(value: String): Akiwrapper.Answer? {
		val textChannel = this.guild.getTextChannelById(this.channelId)

		if (this.checkEnd) {
			when (value.lowercase()) {
				"1", "１" -> {
					textChannel?.sendMessage("おち")?.complete()

					this.isGameEnd = true

					this.isReceivingQuestion = false
					this.checkEnd = false
					this.guess?.confirm()

					this.akiWrapper = null
					this.guild.jda.removeEventListener(this)
				}
				"2", "２" -> {
					this.isReceivingQuestion = false
					this.checkEnd = false

					val guess = this.guess
					if (guess != null) {
						this.query = guess.reject()
					}
				}
			}

			return null
		}

		when (value.lowercase()) {
			"はい", "1", "１" -> return Akiwrapper.Answer.YES
			"いいえ", "2", "２" -> return Akiwrapper.Answer.NO
			"わからない", "分からない", "3", "３" -> return Akiwrapper.Answer.DONT_KNOW
			"たぶんそう", "部分的にそう", "4", "４" -> return Akiwrapper.Answer.PROBABLY
			"たぶん違う", "そうでもない", "5", "５" -> return Akiwrapper.Answer.PROBABLY_NOT
			"もどる", "戻る", "7", "７" -> {
				textChannel?.sendTyping()?.complete()

				val question = this.question
				if (question != null) {
					this.query = question.undoAnswer()
				}

				return null
			}
			"やめる", "8", "８" -> {
				textChannel?.sendMessage(":pray:")?.complete()

				this.isGameEnd = true

				this.akiWrapper = null
				this.guild.jda.removeEventListener(this)
				return null
			}
		}
		return null
	}

	fun setEmbed(question: Question): EmbedBuilder {
		val embedBuilder = EmbedBuilder()
		embedBuilder.setDescription("**" + question.text + "**")
		embedBuilder.setAuthor("進行度: " + question.progression + "% (" + (question.step + 1) + ")")
		embedBuilder.addField("はい", "1", true)
		embedBuilder.addField("いいえ", "2", true)
		embedBuilder.addField("わからない", "3", true)
		embedBuilder.addField("たぶんそう", "4", true)
		embedBuilder.addField("たぶんちがう", "5", true)
		embedBuilder.addField("もどる", "7", true)
		embedBuilder.addField("やめる", "8", true)
		return embedBuilder
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return
		}

		if (event.author.isBot) {
			return
		}

		val guild = event.guild
		val channel = event.channel

		if (guild.id != this.guild.id) {
			return
		}

		if (channel.id != this.channelId) {
			return
		}

		if (!this.isReceivingQuestion) {
			return
		}

		val message = event.message.contentRaw.trim()

		ThreadPool.run {
			val answer = this.parseAnswer(message)

			if (answer != null) {
				channel.sendTyping().complete()

				val question = this.question
				if (question != null) {
					this.query = question.answer(answer)
				}
			}

			this.isReceivingQuestion = false
		}
	}

}
