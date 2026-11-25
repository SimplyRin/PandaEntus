package net.simplyrin.pandaentus.tools

import java.io.File
import java.io.IOException
import java.lang.ProcessBuilder.Redirect

import net.simplyrin.pandaentus.utils.Platform

/**
 * Bot を自分で再起動する。
 *
 * 今の Bot が終了するのを待ってから起動スクリプト (start.sh / start.bat) を実行する、
 * 切り離したプロセスを用意する。終了を待つのは、終了時に保存する config.yml / data.yml を、
 * 新しい Bot が古い内容で読み込まないようにするため。
 * このあと、呼び出し側が System.exit する (終了処理で設定が保存される)。
 */
object Restarter {

	/**
	 * 再起動に使う起動スクリプト。なければ null
	 */
	fun startScript(): File? {
		val script = File(if (Platform.isWindows) "start.bat" else "start.sh").absoluteFile
		return script.takeIf { it.isFile }
	}

	/**
	 * 今のプロセスが終了したあとに起動スクリプトを実行するプロセスを用意する。
	 *
	 * @return 用意できたら true。起動スクリプトがない、または用意できなかったときは false
	 */
	fun scheduleRestart(): Boolean {
		val script = this.startScript() ?: return false
		val folder = script.parentFile
		val pid = ProcessHandle.current().pid()

		val command = if (Platform.isWindows) {
			// PowerShell の単一引用符の中では、' を '' と書く
			val dir = folder.path.replace("'", "''")
			val file = script.path.replace("'", "''")
			listOf("powershell", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-Command",
				"Wait-Process -Id $pid -ErrorAction SilentlyContinue; Start-Process -FilePath '$file' -WorkingDirectory '$dir'")
		} else {
			// nohup: screen などが終了したときの SIGHUP で、このプロセスが道連れにならないようにする
			val dir = shellQuote(folder.path)
			val file = shellQuote(script.name)
			listOf("nohup", "sh", "-c", "while kill -0 $pid 2>/dev/null; do sleep 1; done; cd $dir && exec sh $file")
		}

		return try {
			val process = ProcessBuilder(command)
				.redirectOutput(Redirect.DISCARD)
				.redirectError(Redirect.DISCARD)
				.start()
			// 標準入力は使わない
			process.outputStream.close()
			true
		} catch (e: IOException) {
			println("[Restart] 再起動の用意に失敗しました: " + e.message)
			false
		}
	}

	private fun shellQuote(value: String): String {
		return "'" + value.replace("'", "'\\''") + "'"
	}

}
