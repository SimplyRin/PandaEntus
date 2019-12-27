package net.simplyrin.pandaentus.utils;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.simplyrin.config.Config;
import net.simplyrin.pandaentus.Main;

/**
 * Created by SimplyRin on 2019/09/16.
 *
 * Copyright (c) 2019 SimplyRin
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
public class PoolItems {

	private Main instance;

	private File file;
	private Configuration config;

	public PoolItems(Main instance) {
		this.instance = instance;

		this.file = new File("pool.yml");
		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.config = Config.getConfig(this.file);
	}

	public void save() {
		Config.saveConfig(this.config, this.file);
	}

	public void setItem(String key, String value) {
		key = key.trim();

		System.out.println("Saved: '" + key + "', '" + value + "'");
		this.config.set(key.toLowerCase(), value);
		this.save();
	}

	public String getItem(String key) {
		key = key.toLowerCase();
		key = key.trim();

		System.out.println("Detected key: '" + key + "'");

		String item = this.config.getString(key.toLowerCase());

		System.out.println("Detected value: '" + item + "'");

		if (item.equals("")) {
			System.out.println("Key: '" + key + "' is not found! Return default value");
			return key;
		}

		return item;
	}

	public String getReaction(int i) {
		if (i == 1) {
			return "1⃣";
		}
		if (i == 2) {
			return "2⃣";
		}
		if (i == 3) {
			return "3⃣";
		}
		if (i == 4) {
			return "4⃣";
		}
		if (i == 5) {
			return "5⃣";
		}
		if (i == 6) {
			return "6⃣";
		}
		return "🔄";
	}

}
