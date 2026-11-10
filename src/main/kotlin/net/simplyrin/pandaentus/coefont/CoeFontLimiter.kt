package net.simplyrin.pandaentus.coefont

import java.time.LocalDate
import java.time.ZoneId

import net.simplyrin.pandaentus.PandaEntus

/**
 * CoeFont の 1 日あたりの使用回数の制限 (Bot 全体で 1 日 5 回。日付は日本時間で切り替わる)。
 * 使用回数は data.yml に保存するので、再起動しても残る。
 */
class CoeFontLimiter(private val instance: PandaEntus) {

	companion object {

		const val DAILY_LIMIT = 5

		private const val DATE_KEY = "CoeFont.Usage.Date"
		private const val COUNT_KEY = "CoeFont.Usage.Count"

		private val ZONE = ZoneId.of("Asia/Tokyo")

	}

	/**
	 * 使用回数を 1 回分確保する。今日の上限に達していれば false を返す。
	 */
	fun tryAcquire(): Boolean {
		synchronized(this) {
			val used = this.usedToday()
			if (used >= DAILY_LIMIT) {
				return false
			}

			this.save(used + 1)
			return true
		}
	}

	/**
	 * 確保した使用回数を返す。生成に失敗したときに使う。
	 */
	fun release() {
		synchronized(this) {
			this.save((this.usedToday() - 1).coerceAtLeast(0))
		}
	}

	/**
	 * 今日の残り回数
	 */
	fun remaining(): Int {
		synchronized(this) {
			return (DAILY_LIMIT - this.usedToday()).coerceAtLeast(0)
		}
	}

	private fun today(): String {
		return LocalDate.now(ZONE).toString()
	}

	private fun usedToday(): Int {
		val data = this.instance.dataConfig
		if (data.getString(DATE_KEY, "") != this.today()) {
			return 0
		}

		return data.getInt(COUNT_KEY, 0)
	}

	private fun save(count: Int) {
		val data = this.instance.dataConfig
		synchronized(data) {
			data.set(DATE_KEY, this.today())
			data.set(COUNT_KEY, count)
		}

		this.instance.saveDataConfig()
	}

}
