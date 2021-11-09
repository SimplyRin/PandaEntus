package net.simplyrin.pandaentus.gamemanager.wordwolf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import net.simplyrin.config.Config;
import net.simplyrin.config.Configuration;

/**
 * Created by SimplyRin on 2021/11/09.
 *
 * Copyright (C) 2021 SimplyRin
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
@Getter
public class WordWolfTheme {
	
	private static File folder = new File("wordwolf");

	public static WordWolfTheme getTheme(String name) {
		File file = null;
		for (File value : folder.listFiles()) {
			Configuration config = Config.getConfig(value);
			if (name.equalsIgnoreCase(config.getString("name"))) {
				file = value;
			}
		}
		return getTheme(file);
	}
	
	/**
	 * 保存されているファイル全てからテーマをランダムに取得
	 */
	public static WordWolfTheme getTheme() {
		List<File> files = new ArrayList<>();
		for (File value : folder.listFiles()) {
			files.add(value);
		}
		Collections.shuffle(files);
		return getTheme(files.get(0));
	}
	
	/**
	 * 指定したファイルからテーマをランダムに取得
	 */
	public static WordWolfTheme getTheme(File file) {
		Configuration config = Config.getConfig(file);
		
		WordWolfTheme wwt = new WordWolfTheme();
		wwt.baseTheme = config.getString("name");
		
		List<String> themes = config.getStringList("themes");
		Collections.shuffle(themes);
		
		String[] st = themes.get(0).split("[|]");
		wwt.a = st[0];
		wwt.b = st[1];
		
		return wwt;
	}
	
	private String baseTheme, a, b;

}
