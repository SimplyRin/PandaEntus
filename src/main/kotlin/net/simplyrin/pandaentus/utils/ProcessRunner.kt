package net.simplyrin.pandaentus.utils

import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * 外部コマンドを実行して、出力を受け取る。
 *
 * コマンドはシェルを通さず、引数の配列のまま渡す (シェルの記号が解釈されないので、OS が違っても同じ動きになる)。
 * 出力は UTF-8 として読む。
 */
object ProcessRunner {

	// 出力を溜める上限。これを超えた分は読み捨てる
	private const val MAX_STDOUT_LINES = 2000
	private const val MAX_STDERR_CHARS = 4000

	class Result(val exitCode: Int, val stdout: List<String>, val stderr: String, val timedOut: Boolean) {

		val success: Boolean
			get() = !this.timedOut && this.exitCode == 0

		/**
		 * 失敗した理由 (ログ用)
		 */
		fun describeFailure(): String {
			if (this.timedOut) {
				return "時間切れ"
			}

			return "終了コード " + this.exitCode + " " + this.stderr.trim().takeLast(500)
		}

	}

	/**
	 * コマンドを実行して、終了するまで待つ。起動できなかったときは null を返す。
	 * timeoutSeconds を過ぎたら、子プロセスも含めて強制終了する。
	 */
	@JvmStatic
	fun run(command: List<String>, timeoutSeconds: Long): Result? {
		val process = try {
			ProcessBuilder(command).redirectInput(ProcessBuilder.Redirect.PIPE).start()
		} catch (e: IOException) {
			println("[Process] 起動できませんでした: " + command.firstOrNull() + " (" + e.message + ")")
			return null
		}

		// 標準入力は使わない
		try {
			process.outputStream.close()
		} catch (e: IOException) {
		}

		val stdout = ArrayList<String>()
		val stderr = StringBuilder()

		// 出力を読まないと、出力が溜まった時点でプロセスが止まる。そのため、別スレッドで読み続ける
		val stdoutReader = reader(process.inputStream) { line ->
			synchronized(stdout) {
				if (stdout.size < MAX_STDOUT_LINES) {
					stdout.add(line)
				}
			}
		}
		val stderrReader = reader(process.errorStream) { line ->
			synchronized(stderr) {
				if (stderr.length < MAX_STDERR_CHARS) {
					stderr.append(line).append('\n')
				}
			}
		}

		var timedOut = false
		try {
			if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
				timedOut = true
				process.descendants().forEach { it.destroyForcibly() }
				process.destroyForcibly()
				process.waitFor(10, TimeUnit.SECONDS)
			}
		} catch (e: InterruptedException) {
			process.descendants().forEach { it.destroyForcibly() }
			process.destroyForcibly()
			Thread.currentThread().interrupt()
			return null
		}

		stdoutReader.join(5000)
		stderrReader.join(5000)

		val exitCode = if (process.isAlive) -1 else process.exitValue()

		return synchronized(stdout) {
			Result(exitCode, ArrayList(stdout), synchronized(stderr) { stderr.toString() }, timedOut)
		}
	}

	private fun reader(stream: InputStream, consumer: (String) -> Unit): Thread {
		val thread = Thread {
			try {
				stream.bufferedReader(Charsets.UTF_8).use { reader ->
					reader.forEachLine(consumer)
				}
			} catch (e: IOException) {
				// プロセスを強制終了したときは、ここに来る
			}
		}
		thread.isDaemon = true
		thread.name = "process-output"
		thread.start()
		return thread
	}

}
