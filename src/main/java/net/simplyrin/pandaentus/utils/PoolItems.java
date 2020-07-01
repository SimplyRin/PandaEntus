package net.simplyrin.pandaentus.utils;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.simplyrin.config.Config;
import net.simplyrin.pandaentus.Main;

/**
 * Created by SimplyRin on 2019/09/16.
 *
 * Copyright (C) 2019 SimplyRin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class PoolItems {

	// private Main instance;

	private File file;
	private Configuration config;

	public PoolItems(Main instance) {
		// this.instance = instance;

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
