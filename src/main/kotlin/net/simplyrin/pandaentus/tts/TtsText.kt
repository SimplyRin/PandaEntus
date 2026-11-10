package net.simplyrin.pandaentus.tts

/**
 * 読み上げるメッセージの判定と、読み上げ用のテキストへの整形。
 */
object TtsText {

	// 読み上げる文字数の上限。これを超えた分は読まない
	const val MAX_LENGTH = 80

	private val CODE_BLOCK = Regex("```[\\s\\S]*?```")
	private val SPOILER = Regex("\\|\\|[\\s\\S]*?\\|\\|")
	private val URL = Regex("https?://\\S+")
	private val EMOJI_NAME = Regex(":[A-Za-z0-9_]+:")
	private val LAUGH = Regex("[wWｗＷ]{2,}")
	private val LINE_BREAK = Regex("\\s*\\n\\s*")
	private val WHITESPACE = Regex("\\s+")

	/**
	 * 読み上げないメッセージかどうか。
	 * 先頭が ; か ； のメッセージは読み上げない。コマンド (! で始まる) も読み上げない。
	 */
	@JvmStatic
	fun shouldSkip(raw: String): Boolean {
		val text = raw.trimStart()
		return text.startsWith(";") || text.startsWith("；") || text.startsWith("!")
	}

	/**
	 * Discord のメッセージ (メンションなどは名前に変換済みのもの) を、読み上げ用のテキストに整形する。
	 * 読み上げるものが残らなければ null を返す。
	 */
	@JvmStatic
	fun normalize(content: String): String? {
		var text = content
			.replace(CODE_BLOCK, "コードブロック")
			.replace(SPOILER, "")
			.replace(URL, "URL")
			.replace(EMOJI_NAME, "")
			.replace("`", "")
			.replace(LAUGH, "わらわら")
			.replace(LINE_BREAK, "、")
			.replace(WHITESPACE, " ")
			.trim()

		if (text.isEmpty()) {
			return null
		}

		if (text.length > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH) + "、以下略"
		}

		return text
	}

}
