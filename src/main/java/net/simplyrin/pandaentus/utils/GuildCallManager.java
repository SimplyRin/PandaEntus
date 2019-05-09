package net.simplyrin.pandaentus.utils;

import java.util.Date;

import lombok.Getter;
import net.simplyrin.pandaentus.Main;

/**
 * Created by SimplyRin on 2019/04/05.
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
