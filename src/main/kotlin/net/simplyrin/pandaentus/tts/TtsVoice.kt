package net.simplyrin.pandaentus.tts

import java.io.File
import java.util.Locale

/**
 * 読み上げの声。声の種類 (音声モデル)、ピッチ (半音単位の上げ下げ)、話す速さの組み合わせ。
 */
data class TtsVoice(val file: File, val pitch: Double, val rate: Double) {

	companion object {

		// 聞き取りやすい範囲に収める。差が小さいと別の人の声に聞こえないので、間隔を空けている
		private val PITCHES = doubleArrayOf(-3.0, -1.5, 0.0, 1.5, 3.0, 4.5)
		private val RATES = doubleArrayOf(0.9, 1.0, 1.1, 1.2)

		/**
		 * ユーザー ID から声を決める。同じユーザーは、いつでも同じ声になる。
		 * 使える声がなければ null を返す。
		 */
		@JvmStatic
		fun forUser(userId: Long, voices: List<File>): TtsVoice? {
			if (voices.isEmpty()) {
				return null
			}

			// 声の種類・ピッチ・速さを、それぞれ別のハッシュ値から選ぶ (ID の下位桁だけに偏らないようにする)
			val h1 = mix(userId)
			val h2 = mix(h1)
			val h3 = mix(h2)

			return TtsVoice(
				voices[index(h1, voices.size)],
				PITCHES[index(h2, PITCHES.size)],
				RATES[index(h3, RATES.size)]
			)
		}

		private fun index(hash: Long, size: Int): Int {
			return Math.floorMod(hash, size.toLong()).toInt()
		}

		/**
		 * SplitMix64。ユーザー ID (連番に近い値) を、偏りのない値に変換する。
		 */
		private fun mix(value: Long): Long {
			var z = value + 0x9E3779B97F4A7C15uL.toLong()
			z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
			z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
			return z xor (z ushr 31)
		}

	}

	/**
	 * 声の内容を人が読める形で返す。
	 */
	fun describe(): String {
		return String.format(Locale.ROOT, "声: %s / ピッチ: %+.1f / 速さ: ×%.1f", this.file.nameWithoutExtension, this.pitch, this.rate)
	}

}
