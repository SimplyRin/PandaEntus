package net.simplyrin.pandaentus.utils;

import java.util.Date;
import java.util.HashMap;

import lombok.Getter;
import net.simplyrin.pandaentus.Main;

/**
 * Created by SimplyRin on 2019/04/04.
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
public class TimeManager {

	private Main instance;
	private static HashMap<String, TimeManager> maps = new HashMap<>();

	public TimeManager(Main instance) {
		this.instance = instance;
	}

	public TimeManager(Main instance, String userId) {
		this.instance = instance;
	}

	public TimeManager getUser(String userId) {
		if (maps.get(userId) == null) {
			maps.put(userId, new TimeManager(this.instance, userId));
		}
		return maps.get(userId);
	}


	private String userId;
	private Date date;

	@Getter
	private boolean isJoined;

	public void joined() {
		this.date = new Date();
		this.isJoined = true;
	}

	public void quit() {
		this.date = null;
		this.isJoined = false;
		maps.put(this.userId, null);
	}

	public String getJoinedTime() {
		return this.date.getHours() + "時" + this.date.getMinutes() + "分";
	}

	public String getCurrentTime() {
		return this.instance.getUptime(this.date);
	}

}
