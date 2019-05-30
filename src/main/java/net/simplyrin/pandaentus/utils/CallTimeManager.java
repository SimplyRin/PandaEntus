package net.simplyrin.pandaentus.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.Main.Time;

/**
 * Created by SimplyRin on 2019/05/30.
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
public class CallTimeManager {

	private Main instance;
	private String guildId;

	@Getter
	private HashMap<User, CallTime> map = new HashMap<>();

	public CallTimeManager(Main instance, String guildId) {
		this.instance = instance;
		this.guildId = guildId;
	}

	public void join(User user) {
		if (this.map.get(user) == null) {
			this.map.put(user, new CallTime(user));
		}

		this.map.get(user).join();
	}

	public void quit(User user) {
		this.map.get(user).quit();
	}

	@Getter
	public class CallTime {

		private User user;
		private Time time;
		private Date tempJoinedTime;

		public CallTime(User user) {
			this.user = user;
		}

		public void join() {
			this.tempJoinedTime = new Date();
		}

		public void quit() {
			this.time = instance.getTimeFromDate(this.tempJoinedTime);
			this.time.addTime(new Date());
		}

		public Date getDate() {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = null;
			try {
				date = simpleDateFormat.parse(time.getYear() + "/" + time.getMonth() + "/" + time.getDay() + " " + time.getHour() + ":" + time.getMinute() + ":" + time.getSecond());
			} catch (ParseException e) {
			}
			return date;
		}

	}

}