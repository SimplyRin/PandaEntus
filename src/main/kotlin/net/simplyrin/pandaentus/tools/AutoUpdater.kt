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
 */
class AutoUpdater(private val instance: PandaEntus) {

	companion object {

		private const val DEFAULT_TIME = "04:00"
		private const val DEFAULT_ZONE = "Asia/Tokyo"

	}

	private val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
		Thread(runnable, "auto-updater").apply { isDaemon = true }
	}

	fun start() {
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
			println("[AutoUpdate] アップデートの確認に失敗しました。")
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
			println("[AutoUpdate] 最新のリリース ($runNumber) に $jarName が添付されていません。")
			return
		}

		println("[AutoUpdate] アップデートを確認しました。" + Version.GITHUB_RUN_NUMBER + " -> " + runNumber)

		// 再起動できないと、手動で再起動するまで古いまま動き続けるので、jar を置き換えるのは再起動できるときだけにする
		if (Restarter.startScript() == null) {
			println("[AutoUpdate] 起動スクリプト (start.sh / start.bat) がないため、自動では更新しません。!update で更新して、手動で再起動してください。")
			return
		}

		val now = Updater.getRunningJar()
		if (now == null) {
			println("[AutoUpdate] 実行中の jar を特定できないため、更新しません。")
			return
		}

		val file = File(now.parentFile, "PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies-v" + runNumber + ".jar")
		if (!Updater.downloadJar(downloadUrl, file, Updater.getAssetSha256(release, jarName))) {
			file.delete()
			println("[AutoUpdate] ファイルのダウンロードに失敗しました。")
			return
		}

		val pending = Updater.install(file, now)
		println("[AutoUpdate] " + (pending?.name ?: now.name) + " を更新しました。")

		if (!Restarter.scheduleRestart()) {
			println("[AutoUpdate] 再起動を用意できませんでした。手動で再起動してください。")
			return
		}

		println("[AutoUpdate] 再起動します。")
		// 終了処理 (シャットダウンフック) で、設定が保存される
		System.exit(0)
	}

}
