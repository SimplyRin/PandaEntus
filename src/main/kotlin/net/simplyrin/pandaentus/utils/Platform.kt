package net.simplyrin.pandaentus.utils

import java.io.File
import java.util.Locale

/**
 * 実行している OS と CPU の種類。OS ごとに処理を分けるときは、ここを参照する。
 */
object Platform {

	private val osName = System.getProperty("os.name").orEmpty().lowercase(Locale.ROOT)
	private val osArch = System.getProperty("os.arch").orEmpty().lowercase(Locale.ROOT)

	@JvmField
	val isWindows = osName.startsWith("windows")

	@JvmField
	val isMac = osName.startsWith("mac") || osName.contains("darwin")

	@JvmField
	val isLinux = osName.contains("linux")

	@JvmField
	val isArm64 = osArch == "aarch64" || osArch == "arm64"

	@JvmField
	val isX64 = osArch == "amd64" || osArch == "x86_64"

	/**
	 * 実行ファイルの名前 (Windows では .exe を付ける)
	 */
	@JvmStatic
	fun executable(name: String): String {
		return if (isWindows) "$name.exe" else name
	}

	/**
	 * 環境変数 PATH から実行ファイルを探す。見つからなければ null を返す。
	 */
	@JvmStatic
	fun findOnPath(name: String): File? {
		val path = System.getenv("PATH") ?: return null
		val fileName = executable(name)

		return path.split(File.pathSeparator)
			.filter { it.isNotBlank() }
			.map { File(it.trim('"'), fileName) }
			.firstOrNull { it.isFile && it.canExecute() }
	}

}
