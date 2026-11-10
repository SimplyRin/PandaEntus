package net.simplyrin.pandaentus.utils

import net.simplyrin.config.Configuration

/**
 * config.yml に混在していた、Bot が実行中に書き込むデータを data.yml へ移す。
 *
 * config.yml には手で設定する項目 (トークン、API キー、プロキシなど) だけを残し、
 * サーバーごとの設定や動画情報のキャッシュなどの Bot のデータは data.yml に保存する。
 */
object DataMigration {

	// 丸ごと Bot のデータになっているセクション
	private val DATA_SECTIONS = listOf("Guild", "User", "Server", "DefaultChannelName", "VoiceOnlyChat", "LocalMessage")

	// YouTube セクションのうち、手で設定する項目。これ以外は動画情報のキャッシュ (動画 ID ごとの Title / Duration / Path)
	private val YOUTUBE_SETTINGS = setOf("OAuth2", "Proxy", "PoToken", "VisitorData")

	/**
	 * config から data へデータを移す。移した項目数を返す (何も移すものがなければ 0)。
	 *
	 * 同じキーが既に data にある場合は data の値を優先し、config 側の値は破棄する。
	 * そのため、古い config.yml を戻して再起動しても data.yml の新しいデータは上書きされない。
	 */
	@JvmStatic
	fun migrate(config: Configuration, data: Configuration): Int {
		var moved = 0

		for (key in DATA_SECTIONS) {
			val value = config.self.remove(key) ?: continue
			moved += mergeInto(data.self, key, value)
		}

		val youtube = config.self["YouTube"] as? Configuration
		if (youtube != null) {
			for (key in youtube.self.keys.toList()) {
				if (key in YOUTUBE_SETTINGS) {
					continue
				}

				val value = youtube.self.remove(key) ?: continue
				val dataYoutube = data.self.getOrPut("YouTube") { Configuration() } as? Configuration ?: continue
				moved += mergeInto(dataYoutube.self, key, value)
			}

			// 手動設定が何も残っていなければ、空の YouTube セクションは消す
			if (youtube.self.isEmpty()) {
				config.self.remove("YouTube")
			}
		}

		return moved
	}

	/**
	 * target[key] へ value を取り込む。セクション同士は再帰的に統合し、値が衝突した場合は target を優先する。
	 * 取り込んだ (追加した) 項目数を返す。
	 */
	private fun mergeInto(target: MutableMap<String, Any>, key: String, value: Any): Int {
		val existing = target[key]
		if (existing == null) {
			target[key] = value
			return 1
		}

		if (existing is Configuration && value is Configuration) {
			var count = 0
			for ((childKey, childValue) in value.self.entries.toList()) {
				if (childValue != null) {
					count += mergeInto(existing.self, childKey, childValue)
				}
			}
			return count
		}

		return 0
	}

}
