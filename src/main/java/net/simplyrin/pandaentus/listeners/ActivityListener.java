package net.simplyrin.pandaentus.listeners;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ActivityListener extends ListenerAdapter {
	
	private final PandaEntus instance;
	
	@Override
	public void onUserActivityStart(UserActivityStartEvent event) {
		var guild = event.getGuild();
		var member = event.getMember();
		
		var activity = event.getNewActivity().asRichPresence();
		
		System.out.println("[ActivityListener] " + member.getEffectiveName() + ": " + activity.getName());
	}
	
	@Override
	public void onUserActivityEnd(UserActivityEndEvent event) {
		
	}

}
