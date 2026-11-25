package net.simplyrin.pandaentus.tools

import java.io.File
import java.time.DateTimeException
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.utils.Version

/**
 * 毎日、深夜 (既定は 4:00) に PandaEntus のアップデートを確認して、あれば更新して自動で再起動する。
 *
 * config.yml:
 * - AutoUpdate.Enabled: false で無効にする (既定は true)
 * - AutoUpdate.Time: 確認する時刻 "HH:mm" (既定は 04:00)
 * - AutoUpdate.Zone: その時刻のタイムゾーン (既定は Asia/Tokyo)
 *
 * GitHub Actions でビルドした jar (Pre-release) が対象。開発者ビルドでは動かない。
 *
 * 状況は Bot オーナーの DM に送る (更新を始めたとき、失敗したとき、再起動して更新が完了したとき)。
 * 更新がなかったときは送らない。
 */
class AutoUpdater(private val instance: PandaEntus) {

	companion object {

		private const val DEFAULT_TIME = "04:00"
		private const val DEFAULT_ZONE = "Asia/Tokyo"

		// 再起動する前に data.yml に書いておき、再起動後に結果を知らせるためのキー
		private const val PENDING_KEY = "AutoUpdate.Pending"

	}

	private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
		Thread(runnable, "auto-updater").apply { isDaemon = true }
	}

	fun start() {
		// 自動アップデートで再起動したあとなら、その結果を知らせる (自動アップデートを止めていても、知らせる)
		this.scheduler.execute {
			try {
				this.reportResult()
			} catch (e: Exception) {
				println("[AutoUpdate] 結果の通知に失敗しました: $e")
			}
		}

		val config = this.instance.config
		if (!config.contains("AutoUpdate.Enabled")) {
			config.set("AutoUpdate.Enabled", true)
		}
		if (!config.contains("AutoUpdate.Time")) {
			config.set("AutoUpdate.Time", DEFAULT_TIME)
		}
		if (!config.contains("AutoUpdate.Zone")) {
			config.set("AutoUpdate.Zone", DEFAULT_ZONE)
		}

		if (!config.getBoolean("AutoUpdate.Enabled", true)) {
			return
		}
		if (Version.GITHUB_RUN_NUMBER.isEmpty()) {
			println("[AutoUpdate] 開発者ビルドのため、自動アップデートは行いません。")
			return
		}

		this.scheduleNext()
	}

	private fun scheduleNext() {
		val config = this.instance.config

		val zone = try {
			ZoneId.of(config.getString("AutoUpdate.Zone", DEFAULT_ZONE))
		} catch (e: DateTimeException) {
			println("[AutoUpdate] AutoUpdate.Zone が正しくないため、$DEFAULT_ZONE を使います。")
			ZoneId.of(DEFAULT_ZONE)
		}
		val time = try {
			LocalTime.parse(config.getString("AutoUpdate.Time", DEFAULT_TIME))
		} catch (e: DateTimeException) {
			println("[AutoUpdate] AutoUpdate.Time が正しくないため、$DEFAULT_TIME を使います。")
			LocalTime.parse(DEFAULT_TIME)
		}

		val now = ZonedDateTime.now(zone)
		var next = now.with(time.withSecond(0).withNano(0))
		if (!next.isAfter(now)) {
			next = now.plusDays(1).with(time.withSecond(0).withNano(0))
		}

		println("[AutoUpdate] 次のアップデート確認: $next")

		this.scheduler.schedule({
			try {
				this.check()
			} catch (e: Exception) {
				println("[AutoUpdate] アップデートに失敗しました: $e")
			} finally {
				// 再起動したときは、ここには戻らない
				this.scheduleNext()
			}
		}, Duration.between(now, next).toMillis().coerceAtLeast(1000L), TimeUnit.MILLISECONDS)
	}

	private fun check() {
		val release = Updater.getLatestRelease()
		if (release == null) {
			this.fail("アップデートの確認に失敗しました。(GitHub に接続できませんでした)")
			return
		}

		val runNumber = Updater.getRunNumber(release)
		if (runNumber.equals(Version.GITHUB_RUN_NUMBER, ignoreCase = true)) {
			println("[AutoUpdate] 最新のバージョン ($runNumber) を使用しています。")
			return
		}

		// 古いリリースに戻してしまわないようにする
		val current = Version.GITHUB_RUN_NUMBER.toIntOrNull()
		val latest = runNumber.toIntOrNull()
		if (current != null && latest != null && latest < current) {
			println("[AutoUpdate] 最新のリリース ($runNumber) が使用中のバージョン ($current) より古いため、更新しません。")
			return
		}

		val jarName = Updater.getJarName()
		val downloadUrl = Updater.getAssetUrl(release, jarName)
		if (downloadUrl == null) {
			this.fail("最新のリリース ($runNumber) に $jarName が添付されていません。")
			return
		}

		println("[AutoUpdate] アップデートを確認しました。" + Version.GITHUB_RUN_NUMBER + " -> " + runNumber)

		// 再起動できないと、手動で再起動するまで古いまま動き続けるので、jar を置き換えるのは再起動できるときだけにする
		if (Restarter.startScript(this.instance.config) == null) {
			this.fail("新しいバージョン ($runNumber) がありますが、起動スクリプトがないため自動では更新しません。" +
				"!update で更新して、手動で再起動してください。\n" + release.get("html_url")?.asString.orEmpty())
			return
		}

		val now = Updater.getRunningJar()
		if (now == null) {
			this.fail("実行中の jar を特定できないため、更新しません。")
			return
		}

		val file = File(now.parentFile, "PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies-v" + runNumber + ".jar")
		if (!Updater.downloadJar(downloadUrl, file, Updater.getAssetSha256(release, jarName))) {
			file.delete()
			this.fail("新しいバージョン ($runNumber) のダウンロードに失敗しました。")
			return
		}

		val pending = Updater.install(file, now)
		println("[AutoUpdate] " + (pending?.name ?: now.name) + " を更新しました。")

		if (!Restarter.scheduleRestart(this.instance.config)) {
			this.fail("新しいバージョン ($runNumber) に更新しましたが、再起動を用意できませんでした。手動で再起動してください。")
			return
		}

		// 再起動したあとに、更新が終わったことを知らせるための記録。再起動の前に確実に保存しておく
		val data = this.instance.dataConfig
		synchronized(data) {
			data.set("$PENDING_KEY.From", Version.GITHUB_RUN_NUMBER)
			data.set("$PENDING_KEY.To", runNumber)
		}
		this.instance.saveDataConfig()

		println("[AutoUpdate] 再起動します。")
		this.instance.notifyBotOwners("🔄 **自動アップデート**\n" +
			"新しいバージョンをインストールしました。再起動します。\n" +
			"現在: **" + Version.GITHUB_RUN_NUMBER + "** → 最新: **" + runNumber + "**\n" +
			release.get("html_url")?.asString.orEmpty())

		// 終了処理 (シャットダウンフック) で、設定が保存される
		System.exit(0)
	}

	/**
	 * 自動アップデートのあとの起動なら、更新できたかどうかを Bot オーナーに知らせる。
	 */
	private fun reportResult() {
		val data = this.instance.dataConfig
		val from: String
		val to: String
		synchronized(data) {
			if (!data.contains(PENDING_KEY)) {
				return
			}
			from = data.getString("$PENDING_KEY.From", "")
			to = data.getString("$PENDING_KEY.To", "")
			data.set(PENDING_KEY, null)
		}
		this.instance.saveDataConfig()

		if (Version.GITHUB_RUN_NUMBER.equals(to, ignoreCase = true)) {
			println("[AutoUpdate] $from -> $to の更新が完了しました。")
			this.instance.notifyBotOwners("✅ **自動アップデート**\n更新が完了しました。バージョン **$from** → **$to** で起動しました。")
		} else {
			println("[AutoUpdate] $from -> $to に更新するはずでしたが、" + Version.GITHUB_RUN_NUMBER + " で起動しています。")
			this.instance.notifyBotOwners("⚠️ **自動アップデート**\n" +
				"**$to** に更新するはずでしたが、**" + Version.GITHUB_RUN_NUMBER.ifEmpty { "開発者ビルド" } + "** で起動しています。" +
				"jar の置き換えか、起動スクリプトを確認してください。")
		}
	}

	/**
	 * 失敗をログに出して、Bot オーナーにも知らせる。
	 */
	private fun fail(message: String) {
		println("[AutoUpdate] $message")
		this.instance.notifyBotOwners("⚠️ **自動アップデート**\n$message")
	}

}
