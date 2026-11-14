package net.simplyrin.pandaentus.utils

import java.net.URI
import java.util.Locale

/**
 * ユーザーが入力した URL を、外部コマンド (yt-dlp) に渡してよいか確認する。
 */
object SafeUrl {

	/**
	 * text が domain (またはそのサブドメイン) の https の URL であれば、その URL を返す。そうでなければ null を返す。
	 */
	@JvmStatic
	fun parse(text: String, domain: String): String? {
		val trimmed = text.trim()

		val uri = try {
			URI(trimmed)
		} catch (e: Exception) {
			return null
		}

		if (!"https".equals(uri.scheme, ignoreCase = true) || uri.userInfo != null) {
			return null
		}

		val host = uri.host?.lowercase(Locale.ROOT) ?: return null
		if (host != domain && !host.endsWith(".$domain")) {
			return null
		}

		return uri.toString()
	}

}
