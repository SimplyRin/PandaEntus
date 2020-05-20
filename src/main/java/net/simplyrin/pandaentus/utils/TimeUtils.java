package net.simplyrin.pandaentus.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by SimplyRin on 2020/05/20.
 *
 * Copyright (c) 2020 SimplyRin
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
			System.out.println("Checking " + callTime.key);
			if (callTime.key.startsWith(guildId)) {
				list.add(callTime);
			}
		}
		return list;
	}

	public Collection<CallTime> getList() {
		return this.callTime.values();
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

		public void join() {
			this.joined = System.currentTimeMillis();
		}

		public void quit() {
			long end = (System.currentTimeMillis() - this.joined) / 1000;
			this.joined = 0;

			this.totalTime += end;
		}

		public String getName() {
			return this.name;
		}

		public String getTime() {
			String print = "";

			System.out.println("Total Time: " + this.totalTime);

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
				print += "0秒 ";
			}

			return print;
		}

	}

}
