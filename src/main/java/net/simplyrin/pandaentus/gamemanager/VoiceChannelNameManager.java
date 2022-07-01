package net.simplyrin.pandaentus.gamemanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.simplyrin.pandaentus.PandaEntus;

/**
 * Created by SimplyRin on 2022/06/30.
 *
 * Copyright (c) 2022 SimplyRin
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
@Getter
@RequiredArgsConstructor
public class VoiceChannelNameManager {
	
	private final PandaEntus instance;
	
	private HashMap<String, List<String>> map = new HashMap<>();

	// Member ID -> Channel ID
	private HashMap<String, Long> joinedChannel = new HashMap<>();
	
	public void updateVoiceChannelName(Member member) {
		var voiceState = member.getVoiceState();
		if (voiceState == null) {
			return;
		}
		
		var channel = voiceState.getChannel();
		if (channel == null) {
			return;
		}
		
		for (Member m : channel.getMembers()) {
			var activity = m.getActivities();
			
			for (Activity a : activity) {
				this.updateVoiceChannelName(m, a);
			}
		}
	}
	
	public void updateVoiceChannelName(Member member, Activity activity) {
		this.updateVoiceChannelName(member, activity != null ? activity.getName() : null);
	}
	
	public void updateVoiceChannelName(Member member, String gameList) {
		this.updateVoiceChannelName(member, Arrays.asList(gameList));
	}

	public void updateVoiceChannelName(Member member, List<String> lastGame) {
		System.out.println(member.getEffectiveName() + ": Activity: " + lastGame);
		
		var channelId = this.joinedChannel.get(member.getGuild().getId() + "-" + member.getId());

		if (channelId != null) {
			var defaultName = instance.getConfig().getString("DefaultChannelName." + channelId + ".Default", null);
			if (defaultName == null) {
				return;
			}
			
			var channel = member.getGuild().getVoiceChannelById(channelId);
			
			var key = channel.getId() + "," + member.getId();
			
			this.map.put(key, lastGame);
			
			// 更新
			var members = channel.getMembers();
			var size = members.size();
			
			System.out.println(channel.getId() + ": " + this.map.values().toString());
			
			// ゲーム割合
			// ゲーム名 -> プレイ人数
			HashMap<String, Integer> games = new HashMap<>();
			
			for (var entry : this.map.entrySet()) {
				if (entry.getKey().startsWith(String.valueOf(channelId))) {
					if (lastGame != null) {
						for (String lG : lastGame) {
							if (games.get(lG) == null) {
								games.put(lG, 1);
							} else {
								games.put(lG, games.get(lG) + 1);
							}
						}
					}
				}
			}
			
			System.out.println(channelId + "(" + size + "): " + defaultName);
			
			var sortList = this.sort(games);
			
			if (size > 0 && sortList != null && sortList.size() > 0) {
				var item = sortList.get(0);
				
				if (item != null) {
					member.getGuild().getVoiceChannelById(channelId).getManager().setName((sortList.size() == 1 ? "" : "?") + item.getKey()).complete();
				}
			} else {
				member.getGuild().getVoiceChannelById(channelId).getManager().setName(defaultName).complete();
			}
		}
	}
	
	public List<Entry<String, Integer>> sort(HashMap<String, Integer> map) {
		var list = new ArrayList<Entry<String, Integer>>(map.entrySet());
		
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});
		
		return list;
	}

}
