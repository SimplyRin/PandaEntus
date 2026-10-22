package net.simplyrin.pandaentus.listeners;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import net.dv8tion.jda.api.events.user.UserActivityEndEvent;
import net.dv8tion.jda.api.events.user.UserActivityStartEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.PandaEntus;

/**
 * Created by SimplyRin on 2022/04/03.
 *
 * Copyright (C) 2022 SimplyRin
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
@Getter
public class ActivityListener extends ListenerAdapter {
	
	private final PandaEntus instance;
	
	public ActivityListener(PandaEntus instance) {
		this.instance = instance;
		
		this.startManager();
	}
	
	// イベントスレッドと監視スレッドから同時にアクセスされるため並行安全なコレクションを使う
	private Map<String, List<String>> map = new ConcurrentHashMap<>();
	
	public void startManager() {
		new Thread(() -> {
			while (true) {
				for (var entry : this.map.entrySet()) {
					// GuildId-UserId
					var id = entry.getKey();
					
					var guildId = id.split("[-]")[0];
					var userId = id.split("[-]")[1];
					
					var enabled = this.instance.getConfig().getBoolean("Guild." + guildId + "." + userId + ".IsEnabledActivity", false);
					if (!enabled) {
						continue;
					}
					
					var gameList = entry.getValue();
					
					for (String game : gameList) {
						var configKey = "Guild." + guildId + "." + userId + ".Game." + game;
						
						var playedMinutes = this.instance.getActivityConfig().getInt(configKey, 0);
						
						playedMinutes++;
						
						this.instance.getActivityConfig().set(configKey, playedMinutes);
					}
				}
				
				this.instance.saveActivityConfig();
				
				try {
					TimeUnit.MINUTES.sleep(1);
				} catch (Exception e) {
				}
			}
		}).start();
	}
	
	@Override
	public void onUserActivityStart(UserActivityStartEvent event) {
		var guild = event.getGuild();
		var member = event.getMember();
		var activity = event.getNewActivity();

		if (member.getUser().isBot()) {
			return;
		}
		
		// this.instance.getVcNameManager().updateVoiceChannelName(member, activity);

		var games = this.map.computeIfAbsent(guild.getId() + "-" + member.getId(), key -> new CopyOnWriteArrayList<>());
		if (!games.contains(activity.getName())) {
			games.add(activity.getName());
		}

		System.out.println("[ActivityListener-START] " + member.getEffectiveName() + "@" + guild.getId() + ": " + activity.getName());
	}
	
	@Override
	public void onUserActivityEnd(UserActivityEndEvent event) {
		var guild = event.getGuild();
		var member = event.getMember();

		if (member.getUser().isBot()) {
			return;
		}

		try {
			// this.instance.getVcNameManager().updateVoiceChannelName(member);
		} catch (Exception e) {

		}


		var activity = event.getOldActivity();
		
		var games = this.map.computeIfAbsent(guild.getId() + "-" + member.getId(), key -> new CopyOnWriteArrayList<>());
		games.remove(activity.getName());
		
		System.out.println("[ActivityListener-END] " + member.getEffectiveName() + "@" + guild.getId() + ": " + activity.getName());
	}

}
