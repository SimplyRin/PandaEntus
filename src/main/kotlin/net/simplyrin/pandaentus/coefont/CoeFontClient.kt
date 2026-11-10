package net.simplyrin.pandaentus.coefont

import java.io.File
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration

import com.google.gson.JsonObject
import com.google.gson.JsonParser

import net.simplyrin.config.Configuration

/**
 * CoeFont CLOUD のメーカーページ (https://coefont.cloud/maker/hiroyuki) の「ひろゆき」の声で、テキストから音声 (wav) を生成する。
 *
 * メーカーページがブラウザから呼んでいる、公開 API ではない口を使っている。そのため、仕様が予告なく変わったり、
 * アクセスを制限されたりする可能性がある。通信は config.yml の YouTube.Proxy (あれば) を経由する。
 */
class CoeFontClient(config: Configuration) {

	companion object {

		// ひろゆきの CoeFont ID と、メーカーページが使っている variant
		private const val COEFONT_ID = "19d55439-312d-4a1d-a27b-28f0f31bedc5"
		private const val VARIANT = "maker-tts-v2"

		private const val ENDPOINT = "https://backend.coefont.cloud/coefonts/$COEFONT_ID/try"

		private const val USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/156.0.0.0 Safari/537.36"

		// メーカーページと同じ制限 (1 文字以上、140 文字未満)
		const val MAX_LENGTH = 139

		// 生成に失敗した理由。ユーザーに表示する
		class GenerateException(message: String, cause: Throwable? = null) : Exception(message, cause)

	}

	private val client: HttpClient

	init {
		val builder = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)

		val proxyHost: String? = config.getString("YouTube.Proxy.Host", null)
		val proxyPort = config.getInt("YouTube.Proxy.Port", 0)
		if (!proxyHost.isNullOrEmpty() && proxyPort > 0) {
			builder.proxy(ProxySelector.of(InetSocketAddress(proxyHost, proxyPort)))
			println("CoeFont へのアクセスを $proxyHost:$proxyPort 経由に設定しました。")
		}

		this.client = builder.build()
	}

	/**
	 * text を読み上げた音声を output (wav) に保存する。失敗したら [GenerateException] を投げる。
	 */
	fun generate(text: String, output: File) {
		val location = this.requestLocation(text)
		this.download(location, output)
	}

	private fun requestLocation(text: String): String {
		val body = JsonObject()
		body.addProperty("variant", VARIANT)
		body.addProperty("text", text)

		val request = HttpRequest.newBuilder(URI.create(ENDPOINT))
			.timeout(Duration.ofSeconds(60))
			.header("Content-Type", "application/json")
			.header("User-Agent", USER_AGENT)
			.header("Origin", "https://coefont.cloud")
			.header("Referer", "https://coefont.cloud/")
			.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
			.build()

		val response = try {
			this.client.send(request, HttpResponse.BodyHandlers.ofString())
		} catch (e: IOException) {
			throw GenerateException("CoeFont に接続できませんでした。", e)
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			throw GenerateException("生成が中断されました。", e)
		}

		val json = try {
			JsonParser.parseString(response.body()).asJsonObject
		} catch (e: Exception) {
			throw GenerateException("CoeFont の応答を読み取れませんでした。(HTTP " + response.statusCode() + ")", e)
		}

		if (json.get("message")?.asString == "NG word") {
			throw GenerateException("文章に NG ワードが含まれているため、生成できませんでした。")
		}

		val location = json.get("location")?.asString
		if (response.statusCode() !in 200..299 || location.isNullOrEmpty()) {
			println("[CoeFont] 生成に失敗しました: HTTP " + response.statusCode() + " " + response.body().take(200))
			throw GenerateException("CoeFont が混み合っているなどで、生成に失敗しました。時間をおいて試してください。")
		}

		return location
	}

	private fun download(location: String, output: File) {
		val request = HttpRequest.newBuilder(URI.create(location))
			.timeout(Duration.ofSeconds(60))
			.header("User-Agent", USER_AGENT)
			.GET()
			.build()

		try {
			val response = this.client.send(request, HttpResponse.BodyHandlers.ofInputStream())
			response.body().use { input ->
				if (response.statusCode() !in 200..299) {
					throw GenerateException("生成した音声を取得できませんでした。(HTTP " + response.statusCode() + ")")
				}

				Files.copy(input, output.toPath(), StandardCopyOption.REPLACE_EXISTING)
			}
		} catch (e: IOException) {
			throw GenerateException("生成した音声を取得できませんでした。", e)
		} catch (e: InterruptedException) {
			Thread.currentThread().interrupt()
			throw GenerateException("生成が中断されました。", e)
		}
	}

}
