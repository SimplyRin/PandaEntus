package net.simplyrin.pandaentus.utils;

import java.util.Date;

import lombok.Getter;
import net.simplyrin.pandaentus.Main;

/**
 * Created by SimplyRin on 2019/04/05.
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
public class GuildCallManager {

	private Main instance;
	private String channelId;

	public GuildCallManager(Main instance, String channelId) {
		this.instance = instance;
		this.channelId = channelId;
	}

	@Getter
	private boolean isCalling;
	private String joinUserId;
	private Date date;

	public void joined(String joinUserId) {
		this.isCalling = true;
		this.joinUserId = joinUserId;
		this.date = new Date();
	}

	public void quit() {
		this.isCalling = false;
		this.joinUserId = null;
		this.instance.getGuildCallMaps().put(this.channelId, null);
	}

	public String getJoinUserId() throws IllegalArgumentException {
		if (this.joinUserId == null) {
			throw new IllegalArgumentException();
		}
		return this.joinUserId;
	}

	public String getJoinedTime() {
		return this.date.getHours() + "時" + this.date.getMinutes() + "分";
	}

	public String getCurrentTime() {
		return this.instance.getUptime(this.date);
	}

}
