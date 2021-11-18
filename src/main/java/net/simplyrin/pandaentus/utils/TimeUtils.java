package net.simplyrin.pandaentus.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SimplyRin on 2020/05/20.
 *
 * Copyright (C) 2020 SimplyRin
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
public class TimeUtils {

	private HashMap<String, CallTime> callTime = new HashMap<>();

	public CallTime get(String guildId, String userId) {
		String key = guildId + "," + userId;
		if (this.callTime.get(key) == null) {
			this.callTime.put(key, new CallTime(key, userId));
		}
		return this.callTime.get(key);
	}

	public void resetGuild(String guildId) {
		for (CallTime callTime : this.getList()) {
			if (callTime.key.startsWith(guildId)) {
				this.callTime.remove(callTime.key);
			}
		}
	}

	public List<CallTime> getList(String guildId) {
		List<CallTime> list = new ArrayList<>();
		for (CallTime callTime : this.getList()) {
			System.out.println("Checking " + callTime.key + ", " + callTime.totalTime);
			if (callTime.key.startsWith(guildId)) {
				System.out.println("[ADDED] - Checking " + callTime.key + ", " + callTime.totalTime);
				list.add(callTime);
			}
		}
		return list;
	}

	public List<CallTime> getList() {
		List<CallTime> list = new ArrayList<>();

		for (CallTime callTime : this.callTime.values()) {
			list.add(callTime);
		}

		return list;
	}

	public class CallTime {

		private String key;
		private String name;

		public CallTime(String key, String name) {
			this.key = key;
			this.name = name;
		}

		private int totalTime = 0;
		private long joined = 0;
		
		private boolean alreadyJoined = false;

		public void join() {
			if (this.alreadyJoined) {
				return;
			}
			
			this.alreadyJoined = true;
			this.joined = System.currentTimeMillis();
		}

		public void quit() {
			this.alreadyJoined = false;
			
			if (this.joined == 0) {
				return;
			}
			long end = (System.currentTimeMillis() - this.joined) / 1000;
			this.joined = 0;

			this.totalTime += end;
		}

		public String getName() {
			return this.name;
		}

		public String getTime() {
			String print = "";

			long hour = this.totalTime / 3600;
			if (hour >= 1) {
				print += hour + "時間 ";
			}

			long minute = this.totalTime % 3600 / 60;
			if (minute >= 1) {
				print += minute + "分 ";
			} else if (hour >= 1) {
				print += "0分 ";
			}

			long seconds = this.totalTime % 3600 % 60;
			if (seconds >= 1) {
				print += seconds + "秒";
			} else if (hour >= 1 || minute >= 1) {
				print += "0秒";
			}

			if (print.length() == 0) {
				print = "0秒";
			}

			return print;
		}

	}

}
