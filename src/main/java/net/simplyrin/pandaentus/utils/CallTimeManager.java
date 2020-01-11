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
		if (this.map.get(user) != null) {
			this.map.get(user).quit();
		}
	}

	@Getter
	public class CallTime {

		private User user;
		private Time time;
		private Date tempJoinedTime;

		public CallTime(User user) {
			this.user = user;
			this.time = new Main.Time();
		}

		public void join() {
			if (this.time == null) {
				this.time = new Main.Time();
			}
			this.tempJoinedTime = new Date();
		}

		public void resetTime() {
			this.time = null;
		}

		public void quit() {
			/*try {
				Time addedTime = instance.addTime(this.time, instance.dateToTime(new Date()));
				this.time.addTime(addedTime);
			} catch (Exception e) {
				this.time = instance.getTimeFromDate(this.tempJoinedTime);
			}*/

			Time t1 = instance.getTimeFromDate(this.tempJoinedTime);

			time.setYear(time.getYear() + t1.getYear());
			time.setMonth(time.getMonth() + t1.getMonth());
			time.setDay(time.getDay() + t1.getDay());
			time.setHour(time.getHour() + t1.getHour());
			time.setMinute(time.getMinute() + t1.getMinute());
			time.setSecond(time.getSecond() + t1.getSecond());

			if (time.getSecond() >= 60) {
				time.setSecond(time.getSecond() - 60);
				time.setMinute(time.getMinute() + 1);
			}
			if (time.getMinute() >= 60) {
				time.setMinute(time.getMinute() - 60);
				time.setHour(time.getMinute() + 1);
			}
			if (time.getHour() > 24) {
				time.setMinute(time.getMinute() - 24);
				time.setDay(time.getDay() + 1);
			}

			System.out.println(user.getName() + " quit " + time.getHour() + ":" + time.getMinute() + ":" + time.getSecond());

			this.tempJoinedTime = null;
		}

		public Date getDate() {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = null;
			try {
				date = simpleDateFormat.parse(time.getYear() + "/" + time.getMonth() + "/" + time.getDay() + " " + time.getHour() + ":" + time.getMinute() + ":" + time.getSecond());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return date;
		}

	}

}
