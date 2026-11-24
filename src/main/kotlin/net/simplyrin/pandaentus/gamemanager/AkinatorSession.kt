package net.simplyrin.pandaentus.gamemanager

import java.util.concurrent.atomic.AtomicBoolean

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.simplyrin.pandaentus.utils.ThreadPool
import org.eu.zajc.akiwrapper.Akiwrapper
import org.eu.zajc.akiwrapper.AkiwrapperBuilder
import org.eu.zajc.akiwrapper.core.entities.Guess
import org.eu.zajc.akiwrapper.core.entities.Query
import org.eu.zajc.akiwrapper.core.entities.Question

/**
 * アキネーター 1 ゲーム分の状態
 *
 * - 全体プレイ (isPublic = true): チャンネルに質問を投稿し、誰でもボタンか発言 (番号など) で回答できる
 * - 個人プレイ (isPublic = false): 開始ユーザーだけに見えるメッセージ (ephemeral) 上で進行し、
 *   最後に結果をチャンネルへ共有するか聞く
 *
 * Akinator への通信はブロッキングなので、入力は常に ThreadPool 上で 1 件ずつ処理する (busy フラグで排他)
 */
class AkinatorSession(
	val id: String,
	val channel: MessageChannel,
	val owner: User,
	private val ownerName: String,
	val isPublic: Boolean,
	private val onEnd: (AkinatorSession) -> Unit,
) {

	enum class State { CONNECTING, PLAYING, AWAITING_SHARE, ENDED }

	enum class Action(val code: String, val label: String, val answer: Akiwrapper.Answer?) {
		YES("y", "はい", Akiwrapper.Answer.YES),
		NO("n", "いいえ", Akiwrapper.Answer.NO),
		DONT_KNOW("d", "わからない", Akiwrapper.Answer.DONT_KNOW),
		PROBABLY("p", "たぶんそう", Akiwrapper.Answer.PROBABLY),
		PROBABLY_NOT("pn", "たぶん違う", Akiwrapper.Answer.PROBABLY_NOT),
		BACK("back", "もどる", null),
		QUIT("quit", "やめる", null),
		SHARE("share", "チャンネルに共有する", null),
		NO_SHARE("noshare", "共有しない", null);

		companion object {
			fun fromCode(code: String): Action? = entries.firstOrNull { it.code == code }

			/**
			 * 全体プレイでのチャットによる回答
			 */
			fun fromText(text: String): Action? {
				return when (text.trim().lowercase()) {
					"はい", "1", "１" -> YES
					"いいえ", "2", "２" -> NO
					"わからない", "分からない", "3", "３" -> DONT_KNOW
					"たぶんそう", "部分的にそう", "4", "４" -> PROBABLY
					"たぶん違う", "たぶんちがう", "そうでもない", "5", "５" -> PROBABLY_NOT
					"もどる", "戻る", "7", "７" -> BACK
					"やめる", "8", "８" -> QUIT
					else -> null
				}
			}
		}
	}

	private class Result(
		val won: Boolean,
		val name: String?,
		val description: String?,
		val imageUrl: String?,
		val questionCount: Int,
	)

	@Volatile
	var state = State.CONNECTING
		private set

	@Volatile
	var lastActivity = System.currentTimeMillis()
		private set

	/** 処理中の入力があるか。true の間は他の入力を受け付けない */
	private val busy = AtomicBoolean(false)

	// 以下は busy を取得したスレッドからのみ読み書きする
	private var query: Query? = null

	/** 表示中の質問の番号。古いメッセージのボタンが押された場合の判定に使う */
	@Volatile
	private var turn = 0

	/** 全体プレイ: 回答待ちの質問メッセージ */
	private var currentMessageId: String? = null
	private var currentEmbed: MessageEmbed? = null

	/** 個人プレイ: ephemeral メッセージを編集するための最新の InteractionHook (15 分で失効する) */
	@Volatile
	private var lastHook: InteractionHook? = null

	private var result: Result? = null

	val isEnded: Boolean
		get() = this.state == State.ENDED

	/**
	 * Akinator に接続して最初の質問を表示する。ThreadPool から呼ぶこと
	 *
	 * @param hook 個人プレイの場合、表示先の ephemeral メッセージの InteractionHook
	 */
	fun start(hook: InteractionHook?) {
		if (!this.busy.compareAndSet(false, true)) {
			return
		}

		try {
			this.lastHook = hook

			val akiwrapper = AkiwrapperBuilder()
				.setFilterProfanity(false)
				.setLanguage(Akiwrapper.Language.JAPANESE)
				.build()

			val first = akiwrapper.currentQuery ?: throw IllegalStateException("最初の質問を取得できませんでした")
			this.query = first
			this.state = State.PLAYING
			this.touch()
			this.showQuery(first)
		} catch (e: Exception) {
			e.printStackTrace()

			val message = "**アキネーターに接続できませんでした。**\n時間をおいてもう一度お試しください。\n-# " + describe(e)
			this.end()
			if (this.isPublic) {
				this.channel.sendMessage(message).queue(null) { it.printStackTrace() }
			} else {
				this.editPrivate(MessageEditBuilder().setContent(message).setEmbeds(emptyList()).setComponents(emptyList()).build())
			}
		} finally {
			this.busy.set(false)
		}
	}

	/**
	 * ボタンが押されたとき (JDA のイベントスレッドから呼ばれる)
	 */
	fun onButton(event: ButtonInteractionEvent, turn: Int, action: Action) {
		if (this.isEnded) {
			event.reply("このアキネーターはすでに終了しています。").setEphemeral(true).queue()
			return
		}
		if (turn != this.turn) {
			event.reply("この質問にはすでに回答済みです。").setEphemeral(true).queue()
			return
		}
		if (!this.busy.compareAndSet(false, true)) {
			event.reply("処理中です。少し待ってからもう一度押してください。").setEphemeral(true).queue()
			return
		}

		event.deferEdit().queue()

		val hook = event.hook
		if (!this.isPublic) {
			this.lastHook = hook
		}

		ThreadPool.run {
			try {
				this.process(action, event.user) { notice ->
					hook.sendMessage(notice).setEphemeral(true).queue(null) { it.printStackTrace() }
				}
			} catch (e: Exception) {
				e.printStackTrace()
				hook.sendMessage("アキネーターとの通信に失敗しました。もう一度お試しください。\n-# " + describe(e))
					.setEphemeral(true).queue(null) { it.printStackTrace() }
			} finally {
				this.busy.set(false)
			}
		}
	}

	/**
	 * 全体プレイで回答らしい発言があったとき
	 */
	fun onText(user: User, text: String) {
		if (!this.isPublic || this.state != State.PLAYING) {
			return
		}
		val action = Action.fromText(text) ?: return

		if (!this.busy.compareAndSet(false, true)) {
			return
		}

		ThreadPool.run {
			try {
				this.process(action, user) { notice ->
					this.channel.sendMessage(notice).queue(null) { it.printStackTrace() }
				}
			} catch (e: Exception) {
				e.printStackTrace()
				this.channel.sendMessage("アキネーターとの通信に失敗しました。もう一度回答してください。\n-# " + describe(e))
					.queue(null) { it.printStackTrace() }
			} finally {
				this.busy.set(false)
			}
		}
	}

	/**
	 * 一定時間操作がなかった場合などにゲームを終了する。処理中の場合は何もしない
	 *
	 * @return 終了させた場合 true
	 */
	fun terminate(reason: String): Boolean {
		if (!this.busy.compareAndSet(false, true)) {
			return false
		}

		try {
			if (this.isEnded) {
				return true
			}
			this.end()

			if (this.isPublic) {
				this.stripCurrentMessage(null)
				this.channel.sendMessage(reason).queue(null) { it.printStackTrace() }
			} else {
				val builder = MessageEditBuilder().setContent(reason).setComponents(emptyList())
				if (this.result == null) {
					builder.setEmbeds(emptyList())
				}
				this.editPrivate(builder.build())
			}
			return true
		} finally {
			this.busy.set(false)
		}
	}

	private fun process(action: Action, actor: User, notify: (String) -> Unit) {
		this.touch()

		when (this.state) {
			State.PLAYING -> this.processPlay(action, actor, notify)
			State.AWAITING_SHARE -> this.processShare(action)
			else -> {}
		}
	}

	private fun processPlay(action: Action, actor: User, notify: (String) -> Unit) {
		val current = this.query ?: return

		if (action == Action.QUIT) {
			this.quit(actor)
			return
		}

		when (current) {
			is Question -> {
				if (action == Action.BACK) {
					if (current.step == 0) {
						notify("これ以上戻れません。")
						return
					}
					this.advance(current.undoAnswer(), actor, action.label)
					return
				}

				val answer = action.answer ?: return
				val next = current.answer(answer)
				if (next == null) {
					// 候補が尽きて Akinator が降参した
					this.finish(Result(false, null, null, null, current.step + 1), actor, action.label)
				} else {
					this.advance(next, actor, action.label)
				}
			}
			is Guess -> {
				when (action) {
					Action.YES -> {
						current.confirm()
						val description = current.description?.takeUnless { it.isBlank() || it == "---" }
						val result = Result(true, current.name, description, current.image?.toExternalForm(), current.step + 1)
						this.finish(result, actor, "正解！")
					}
					Action.NO -> {
						val next = current.reject()
						if (next == null) {
							this.finish(Result(false, null, null, null, current.step + 1), actor, "違う")
						} else {
							this.advance(next, actor, "違う")
						}
					}
					else -> {}
				}
			}
		}
	}

	private fun processShare(action: Action) {
		val result = this.result ?: return

		when (action) {
			Action.SHARE -> {
				val shared = try {
					this.channel.sendMessageEmbeds(this.buildResultEmbed(result, true)).complete()
					true
				} catch (e: Exception) {
					e.printStackTrace()
					false
				}

				this.end()
				val content = if (shared) "チャンネルに結果を共有しました！" else "チャンネルへの投稿に失敗したため、共有できませんでした。"
				this.editPrivate(MessageEditBuilder().setContent(content).setComponents(emptyList()).build())
			}
			Action.NO_SHARE -> {
				this.end()
				this.editPrivate(MessageEditBuilder().setContent("結果は共有しませんでした。").setComponents(emptyList()).build())
			}
			else -> {}
		}
	}

	private fun advance(next: Query, actor: User, label: String) {
		this.stripCurrentMessage("→ $label (${actorName(actor)})")
		this.query = next
		this.showQuery(next)
	}

	private fun quit(actor: User) {
		this.end()

		if (this.isPublic) {
			this.stripCurrentMessage("→ やめる (${actorName(actor)})")
			this.channel.sendMessage(":pray: アキネーターを終了しました。").queue(null) { it.printStackTrace() }
		} else {
			this.editPrivate(MessageEditBuilder()
				.setContent(":pray: アキネーターを終了しました。")
				.setEmbeds(emptyList())
				.setComponents(emptyList())
				.build())
		}
	}

	private fun finish(result: Result, actor: User, label: String) {
		this.result = result

		if (this.isPublic) {
			this.end()
			this.stripCurrentMessage("→ $label (${actorName(actor)})")
			this.channel.sendMessageEmbeds(this.buildResultEmbed(result, false)).queue(null) { it.printStackTrace() }
			return
		}

		// 個人プレイは結果を見せてから、チャンネルに共有するか聞く
		this.state = State.AWAITING_SHARE
		val turn = ++this.turn
		this.editPrivate(MessageEditBuilder()
			.setContent("この結果をチャンネルに共有しますか？")
			.setEmbeds(this.buildResultEmbed(result, false))
			.setComponents(ActionRow.of(
				Button.primary(this.buttonId(turn, Action.SHARE), Action.SHARE.label),
				Button.secondary(this.buttonId(turn, Action.NO_SHARE), Action.NO_SHARE.label),
			))
			.build())
	}

	private fun showQuery(query: Query) {
		val turn = ++this.turn
		val embed: MessageEmbed
		val rows: List<ActionRow>

		when (query) {
			is Question -> {
				val builder = EmbedBuilder()
				builder.setAuthor("進行度: " + String.format("%.0f", query.progression) + "% (" + (query.step + 1) + " 問目)")
				builder.setDescription("**" + query.text + "**")
				if (this.isPublic) {
					builder.setFooter("ボタン、または番号を発言して回答できます")
				}
				embed = builder.build()

				rows = listOf(
					ActionRow.of(
						Button.success(this.buttonId(turn, Action.YES), this.label(1, Action.YES)),
						Button.danger(this.buttonId(turn, Action.NO), this.label(2, Action.NO)),
						Button.secondary(this.buttonId(turn, Action.DONT_KNOW), this.label(3, Action.DONT_KNOW)),
						Button.primary(this.buttonId(turn, Action.PROBABLY), this.label(4, Action.PROBABLY)),
						Button.primary(this.buttonId(turn, Action.PROBABLY_NOT), this.label(5, Action.PROBABLY_NOT)),
					),
					ActionRow.of(
						Button.secondary(this.buttonId(turn, Action.BACK), this.label(7, Action.BACK)).withDisabled(query.step == 0),
						Button.danger(this.buttonId(turn, Action.QUIT), this.label(8, Action.QUIT)),
					),
				)
			}
			is Guess -> {
				val builder = EmbedBuilder()
				builder.setAuthor("あなたが想像しているのは...")
				builder.setTitle(query.name)
				query.description?.takeUnless { it.isBlank() || it == "---" }?.let { builder.setDescription(it) }
				query.image?.let { builder.setImage(it.toExternalForm()) }
				embed = builder.build()

				rows = listOf(ActionRow.of(
					Button.success(this.buttonId(turn, Action.YES), this.label(1, "正解！")),
					Button.secondary(this.buttonId(turn, Action.NO), this.label(2, "違うので続ける")),
					Button.danger(this.buttonId(turn, Action.QUIT), this.label(8, Action.QUIT)),
				))
			}
			else -> throw IllegalStateException("Unknown query: " + query.javaClass.name)
		}

		if (this.isPublic) {
			val message = this.channel.sendMessageEmbeds(embed).setComponents(rows).complete()
			this.currentMessageId = message.id
			this.currentEmbed = embed
		} else {
			this.editPrivate(MessageEditBuilder().setContent("").setEmbeds(embed).setComponents(rows).build())
		}
	}

	private fun buildResultEmbed(result: Result, forShare: Boolean): MessageEmbed {
		val builder = EmbedBuilder()
		if (forShare) {
			builder.setAuthor(this.ownerName + " さんのアキネーター結果", null, this.owner.effectiveAvatarUrl)
		}

		if (result.won) {
			builder.setTitle("🎉 アキネーターが当てました！")
			val name = result.name ?: "?"
			builder.setDescription("**" + name + "**" + (result.description?.let { "\n$it" } ?: ""))
			result.imageUrl?.let { builder.setImage(it) }
			builder.setFooter(result.questionCount.toString() + " 問目で正解")
		} else {
			builder.setTitle("😵 アキネーターの負けです...")
			builder.setDescription("思い浮かべていたものを当てられませんでした。")
			builder.setFooter(result.questionCount.toString() + " 問で降参")
		}
		return builder.build()
	}

	/**
	 * 全体プレイ: 回答済みの質問メッセージからボタンを外し、誰が何と答えたかを残す
	 */
	private fun stripCurrentMessage(footer: String?) {
		val messageId = this.currentMessageId ?: return
		val embed = this.currentEmbed
		this.currentMessageId = null
		this.currentEmbed = null

		val builder = MessageEditBuilder().setComponents(emptyList())
		if (embed != null && footer != null) {
			builder.setEmbeds(EmbedBuilder(embed).setFooter(footer).build())
		}
		this.channel.editMessageById(messageId, builder.build()).queue(null) { it.printStackTrace() }
	}

	private fun editPrivate(data: MessageEditData) {
		val hook = this.lastHook ?: return
		hook.editOriginal(data).queue(null) { it.printStackTrace() }
	}

	private fun end() {
		this.state = State.ENDED
		this.onEnd(this)
	}

	private fun touch() {
		this.lastActivity = System.currentTimeMillis()
	}

	private fun buttonId(turn: Int, action: Action): String {
		return AkinatorManager.BUTTON_PREFIX + this.id + ":" + turn + ":" + action.code
	}

	private fun label(number: Int, action: Action): String = this.label(number, action.label)

	/**
	 * 全体プレイでは発言で回答する際の番号もボタンに表示する
	 */
	private fun label(number: Int, text: String): String {
		return if (this.isPublic) "$number. $text" else text
	}

	private fun actorName(user: User): String = user.effectiveName

	companion object {

		fun describe(e: Throwable): String {
			val message = e.message
			return if (message.isNullOrBlank()) e.javaClass.simpleName else e.javaClass.simpleName + ": " + message
		}

	}

}
