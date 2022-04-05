package net.simplyrin.pandaentus.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.simplyrin.pandaentus.PandaEntus;

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
@Getter
@RequiredArgsConstructor
public class ImageColor {
	
	private final int r, g, b;
	
	public Color getAsAwtColor() {
		return new Color(r, g, b);
	}
	
	public String getHexColor() {
		return "#" +  (Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b)).toUpperCase();
	}
	
	public static ImageColor getColor(String url) {
		try {
			var connection = (HttpsURLConnection) new URL(url).openConnection();
			connection.addRequestProperty("user-agent", new PandaEntus().getBotUserAgent());
			
			var is = connection.getInputStream();
			var image = ImageIO.read(is);
			
			return getColor(image);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static ImageColor getColor(BufferedImage image) {
		int height = image.getHeight();
		int width = image.getWidth();

		Map<Integer, Integer> m = new HashMap<>();
		for (int i = 0; i < width ; i++) {
			for (int j = 0; j < height ; j++) {
				int rgb = image.getRGB(i, j);
				int[] rgbArr = getRGBArr(rgb);				
		
				if (!isGray(rgbArr)) {				
					var counter = m.get(rgb);   
					if (counter == null) {
						counter = 0;
					}
					counter++;
					m.put(rgb, counter);
				}
			}
		}
		
		var list = new LinkedList<>(m.entrySet());
		
		Collections.sort(list, new Comparator<Entry<Integer, Integer>>() {
			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		
		for (var value : list) {
			var rgb = getRGBArr(value.getKey());
			var ic = new ImageColor(rgb[0], rgb[1], rgb[2]);
			
			System.out.println(ic.getHexColor() + " - " + value.getValue());
		}
		
		ImageColor ic = null;
		
		// わからない (A M)
		for (int i = list.size(); i > 0; i--) {
			var me = list.get(i - 1);
			int[] rgb = getRGBArr(me.getKey());
			
			try {
				Integer.valueOf(Integer.toHexString(rgb[0]));
				
				if (ic == null) {
					ic = new ImageColor(rgb[0], rgb[1], rgb[2]);
					i = 0;
				}
			} catch (Exception e) {
			}
		}
		return ic;
	}

	public static int[] getRGBArr(int pixel) {
		int alpha = (pixel >> 24) & 0xff;
		int red = (pixel >> 16) & 0xff;
		int green = (pixel >> 8) & 0xff;
		int blue = (pixel) & 0xff;
		return new int[] { red, green, blue };
	}

	public static boolean isGray(int[] rgbArr) {
		int rgDiff = rgbArr[0] - rgbArr[1];
		int rbDiff = rgbArr[0] - rgbArr[2];

		int tolerance = 10;
		if (rgDiff > tolerance || rgDiff < -tolerance) {
			if (rbDiff > tolerance || rbDiff < -tolerance) { 
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		try {
			var connection = (HttpsURLConnection) new URL("").openConnection();
			connection.addRequestProperty("user-agent", new PandaEntus().getBotUserAgent());
			
			var is = connection.getInputStream();
			var image = ImageIO.read(is);
			
			var imageColor = ImageColor.getColor(image);
			
			System.out.println(imageColor.r + ", " + imageColor.g + ", " + imageColor.b);
			System.out.println(imageColor.getAsAwtColor());
			System.out.println(imageColor.getHexColor());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
