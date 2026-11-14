package net.simplyrin.pandaentus.tts

import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.Locale
import java.util.concurrent.TimeUnit

import net.simplyrin.config.Configuration
import net.simplyrin.pandaentus.utils.Platform

/**
 * Open JTalk (open_jtalk コマンド) を使って、テキストから音声ファイル (wav) を生成する。
 *
 * config.yml の設定 (省略可):
 * ```
 * OpenJTalk:
 *   Binary: open_jtalk
 *   Dictionary: /var/lib/mecab/dic/open-jtalk/naist-jdic
 *   VoiceDirectories:        # この中の *.htsvoice を全て声として使う (複数あれば、ユーザーごとに声の種類も変わる)
 *     - voices
 *     - /usr/share/hts-voice
 *   Charset: UTF-8           # 辞書の文字コード。Windows 用の Shift_JIS の辞書を使うときは Shift_JIS にする
 * ```
 *
 * 省略したときは、Bot のフォルダ内の open_jtalk/ (bin/open_jtalk, dic, voice)、Debian / Ubuntu、Homebrew (macOS) の順に探す。
 */
class OpenJTalk(
	private val binary: String,
	private val dictionary: String,
	private val voiceDirectories: List<String>,
	private val charset: Charset = Charsets.UTF_8
) {

	companion object {

		const val DEFAULT_BINARY = "open_jtalk"

		// 設定がないときに探す場所。Bot のフォルダ内 (open_jtalk/)、Debian / Ubuntu、Homebrew (macOS) の順
		private val BINARY_CANDIDATES = listOf("open_jtalk/bin/open_jtalk", "open_jtalk/open_jtalk")
		private val DICTIONARY_CANDIDATES = listOf(
			"open_jtalk/dic",
			"/var/lib/mecab/dic/open-jtalk/naist-jdic",
			"/opt/homebrew/opt/open-jtalk/dic",
			"/usr/local/opt/open-jtalk/dic"
		)
		val DEFAULT_VOICE_DIRECTORIES = listOf(
			"voices",
			"open_jtalk/voice",
			"/usr/share/hts-voice",
			"/opt/homebrew/opt/open-jtalk/voice",
			"/usr/local/opt/open-jtalk/voice"
		)

		/**
		 * Bot のフォルダ内に open_jtalk があればそれを、なければ PATH の open_jtalk を使う。
		 */
		private fun defaultBinary(): String {
			return BINARY_CANDIDATES
				.map { File(Platform.executable(it)) }
				.firstOrNull { it.isFile }?.absolutePath
				?: DEFAULT_BINARY
		}

		private fun defaultDictionary(): String {
			return DICTIONARY_CANDIDATES.firstOrNull { File(it).isDirectory } ?: DICTIONARY_CANDIDATES[0]
		}

		// 音声の生成にかける時間の上限
		private const val TIMEOUT_SECONDS = 15L

		// wav ファイルのヘッダの大きさ。これ以下であれば、音声が入っていない
		private const val WAV_HEADER_SIZE = 44L

		@JvmStatic
		fun fromConfig(config: Configuration): OpenJTalk {
			val directories = config.getStringList("OpenJTalk.VoiceDirectories").filter { it.isNotBlank() }

			// 辞書の文字コード。Windows 用に配布されている辞書は Shift_JIS のことがある
			val charsetName = config.getString("OpenJTalk.Charset", "").orEmpty()
			val charset = try {
				if (charsetName.isBlank()) Charsets.UTF_8 else Charset.forName(charsetName)
			} catch (e: Exception) {
				println("[TTS] OpenJTalk.Charset (" + charsetName + ") を使用できないため、UTF-8 を使います。")
				Charsets.UTF_8
			}

			return OpenJTalk(
				config.getString("OpenJTalk.Binary", "").orEmpty().ifBlank { defaultBinary() },
				config.getString("OpenJTalk.Dictionary", "").orEmpty().ifBlank { defaultDictionary() },
				directories.ifEmpty { DEFAULT_VOICE_DIRECTORIES },
				charset
			)
		}

	}

	/**
	 * 使える声 (音声モデル) の一覧。名前順に並べるので、構成が同じなら再起動しても同じ順番になる。
	 * 起動後に追加した声を使うには、再起動が必要。
	 */
	val voices: List<File> by lazy {
		this.voiceDirectories
			.map { File(it) }
			.filter { it.isDirectory }
			.flatMap { directory ->
				directory.walkTopDown()
					.filter { it.isFile && it.extension.equals("htsvoice", ignoreCase = true) }
					.toList()
			}
			.sortedBy { it.name }
			.distinctBy { it.name }
	}

	/**
	 * Open JTalk を実行できる状態かどうか (コマンド、辞書、音声モデルが揃っているか)
	 */
	fun isReady(): Boolean {
		if (!File(this.dictionary).isDirectory || this.voices.isEmpty()) {
			return false
		}

		return try {
			// 引数なしで実行すると、使い方を表示してすぐに終了する
			val process = ProcessBuilder(this.binary)
				.redirectOutput(ProcessBuilder.Redirect.DISCARD)
				.redirectError(ProcessBuilder.Redirect.DISCARD)
				.start()
			process.outputStream.close()
			if (!process.waitFor(5, TimeUnit.SECONDS)) {
				process.destroyForcibly()
			}
			true
		} catch (e: IOException) {
			false
		}
	}

	/**
	 * text を読み上げた音声を output (wav) に生成する。成功したら true を返す。
	 */
	fun synthesize(text: String, voice: TtsVoice, output: File): Boolean {
		val command = listOf(
			this.binary,
			"-x", this.dictionary,
			"-m", voice.file.absolutePath,
			"-fm", format(voice.pitch),
			"-r", format(voice.rate),
			"-ow", output.absolutePath
		)

		return try {
			val process = ProcessBuilder(command)
				.redirectOutput(ProcessBuilder.Redirect.DISCARD)
				.redirectError(ProcessBuilder.Redirect.DISCARD)
				.start()

			// テキストは標準入力で渡す (コマンドラインに含めないので、記号などがオプションとして解釈されない)
			process.outputStream.use { it.write((text + "\n").toByteArray(this.charset)) }

			if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				process.destroyForcibly()
				return false
			}

			process.exitValue() == 0 && output.length() > WAV_HEADER_SIZE
		} catch (e: IOException) {
			e.printStackTrace()
			false
		}
	}

	private fun format(value: Double): String {
		return String.format(Locale.ROOT, "%.2f", value)
	}

}
