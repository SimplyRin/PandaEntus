package net.simplyrin.pandaentus.tts

import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit

import net.simplyrin.config.Configuration

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
 * ```
 */
class OpenJTalk(
	private val binary: String,
	private val dictionary: String,
	private val voiceDirectories: List<String>
) {

	companion object {

		const val DEFAULT_BINARY = "open_jtalk"
		const val DEFAULT_DICTIONARY = "/var/lib/mecab/dic/open-jtalk/naist-jdic"
		val DEFAULT_VOICE_DIRECTORIES = listOf("voices", "/usr/share/hts-voice")

		// 音声の生成にかける時間の上限
		private const val TIMEOUT_SECONDS = 15L

		// wav ファイルのヘッダの大きさ。これ以下であれば、音声が入っていない
		private const val WAV_HEADER_SIZE = 44L

		@JvmStatic
		fun fromConfig(config: Configuration): OpenJTalk {
			val directories = config.getStringList("OpenJTalk.VoiceDirectories").filter { it.isNotBlank() }

			return OpenJTalk(
				config.getString("OpenJTalk.Binary", DEFAULT_BINARY),
				config.getString("OpenJTalk.Dictionary", DEFAULT_DICTIONARY),
				directories.ifEmpty { DEFAULT_VOICE_DIRECTORIES }
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
			process.outputStream.use { it.write((text + "\n").toByteArray(Charsets.UTF_8)) }

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
