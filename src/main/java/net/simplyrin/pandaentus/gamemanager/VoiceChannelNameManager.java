package net.simplyrin.pandaentus.gamemanager;

import java.util.ArrayList;
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
	
	private HashMap<Long, String> map = new HashMap<>();
	private HashMap<Long, HashMap<Long, String>> channelMap = new HashMap<>();
	
	private HashMap<Long, HashMap<String, Integer>> channelGameMap = new HashMap<>();
	
	// Member ID -> Channel ID
	private HashMap<Long, Long> joinedChannel = new HashMap<>();
	
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
		String lastGame = activity != null ? activity.getName() : null;
		
		System.out.println(member.getEffectiveName() + ": Activity: " + lastGame);
		
		var voiceState = member.getVoiceState();
		if (voiceState == null) {
			return;
		}
		
		var channel = member.getGuild().getVoiceChannelById(this.joinedChannel.get(member.getIdLong()));

		var defaultName = instance.getConfig().getString("DefaultChannelName." + channel.getId() + ".Default", null);
		if (defaultName == null) {
			return;
		}
		
		if (this.channelMap.get(channel.getIdLong()) == null) {
			this.channelMap.put(channel.getIdLong(), new HashMap<>());
		}
		
		var map = this.channelMap.get(channel.getIdLong());
		
		map.remove(member.getIdLong());
		if (activity != null) {
			map.put(member.getIdLong(), activity.getName());
		} else {
			map.put(member.getIdLong(), null);
		}
		
		// 更新
		var members = channel.getMembers();
		var size = members.size();
		
		if (this.channelGameMap.get(channel.getIdLong()) == null) {
			this.channelGameMap.put(channel.getIdLong(), new HashMap<>());
		}
		
		System.out.println(channel.getId() + ": " + map.values().toString());
		
		HashMap<String, Integer> gameMap = this.channelGameMap.get(channel.getIdLong());
		
		if (lastGame != null) {
			if (gameMap.get(lastGame) == null) {
				gameMap.put(lastGame, 1);
			}
			
			gameMap.put(lastGame, gameMap.get(activity.getName()) + 1);
		}
		
		if (activity == null) {
			if (this.channelGameMap.get(channel.getIdLong()) != null) {
				if (gameMap.get(lastGame) == null) {
					gameMap.put(lastGame, 1);
				}
				
				if (this.channelMap.get(channel.getIdLong()) != null) {
					String name = this.channelMap.get(channel.getIdLong()).get(member.getIdLong());
				
					if (name != null) {
						gameMap.put(name, gameMap.get(name) - 1);
					}
				}
			}
		}
		
		// ゲーム割合
		var sortList = this.sort(gameMap);
		
		System.out.println(channel.getIdLong() + ": " + defaultName);
		
		if (size >= 1 && sortList != null && sortList.size() >= 1) {
			var item = sortList.get(0);
			
			if (item != null) {
				member.getGuild().getVoiceChannelById(channel.getIdLong()).getManager().setName(item.getKey()).complete();
			}
		} else {
			// instance.getConfig().set("DefaultChannelName." + args[2] + ".Default", null);
			member.getGuild().getVoiceChannelById(channel.getIdLong()).getManager().setName(defaultName).complete();
		}
	}
	
	public List<Entry<String, Integer>> sort(HashMap<String, Integer> map) {
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(map.entrySet());
		
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> a, Entry<String, Integer> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});
		
		return list;
	}
	
	public class GameList {
		private long member;
		private long channel;
		private String gameName;
	}

}
