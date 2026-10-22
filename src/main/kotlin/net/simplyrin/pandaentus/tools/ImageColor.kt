package net.simplyrin.pandaentus.tools

import java.awt.Color
import java.awt.image.BufferedImage
import java.net.URI

import javax.imageio.ImageIO
import javax.net.ssl.HttpsURLConnection

import net.simplyrin.pandaentus.PandaEntus

/**
 * Created by SimplyRin on 2022/04/05.
 *
 * Copyright (c) 2022 SimplyRin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class ImageColor(val r: Int, val g: Int, val b: Int) {

	val asAwtColor: Color
		get() = Color(this.r, this.g, this.b)

	val hexColor: String
		get() = "#" + "%02X%02X%02X".format(this.r, this.g, this.b)

	companion object {

		@JvmStatic
		fun getColor(url: String): ImageColor? {
			try {
				val connection = URI(url).toURL().openConnection() as HttpsURLConnection
				connection.addRequestProperty("user-agent", PandaEntus.BOT_USER_AGENT)

				val image = connection.inputStream.use { ImageIO.read(it) }

				return getColor(image)
			} catch (e: Exception) {
			}
			return null
		}

		@JvmStatic
		fun getColor(image: BufferedImage): ImageColor? {
			val height = image.height
			val width = image.width

			val m = HashMap<Int, Int>()
			for (i in 0 until width) {
				for (j in 0 until height) {
					val rgb = image.getRGB(i, j)
					val rgbArr = getRGBArr(rgb)

					if (!isGray(rgbArr)) {
						m[rgb] = (m[rgb] ?: 0) + 1
					}
				}
			}

			// 出現回数が多い順 (同数の場合は昇順ソート後の逆順) に、赤成分の 16 進表記が数字のみの色を探す
			val sorted = m.entries.sortedBy { it.value }
			for (entry in sorted.asReversed()) {
				val rgb = getRGBArr(entry.key)

				if (Integer.toHexString(rgb[0]).toIntOrNull() != null) {
					return ImageColor(rgb[0], rgb[1], rgb[2])
				}
			}
			return null
		}

		@JvmStatic
		fun getRGBArr(pixel: Int): IntArray {
			// val alpha = (pixel shr 24) and 0xff
			val red = (pixel shr 16) and 0xff
			val green = (pixel shr 8) and 0xff
			val blue = pixel and 0xff
			return intArrayOf(red, green, blue)
		}

		@JvmStatic
		fun isGray(rgbArr: IntArray): Boolean {
			val rgDiff = rgbArr[0] - rgbArr[1]
			val rbDiff = rgbArr[0] - rgbArr[2]

			val tolerance = 10
			if (rgDiff > tolerance || rgDiff < -tolerance) {
				if (rbDiff > tolerance || rbDiff < -tolerance) {
					return false
				}
			}
			return true
		}

	}

}
