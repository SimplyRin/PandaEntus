package net.simplyrin.pandaentus.utils;

import java.util.Date;

import lombok.Getter;
import net.simplyrin.pandaentus.Main;

/**
 * Created by SimplyRin on 2019/04/05.
 *
 * <!-- Apache v2.0 -->
 *  Copyright 2019 SimplyRin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <!-- GPL v3 -->
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
 *
 * <!-- MIT -->
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
