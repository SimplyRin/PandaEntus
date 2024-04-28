package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2022/08/03.
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
public class YouTubeForceLoopCommand extends BaseCommand {
	
	private String command = "!forceloop";
	private String description = "通話に Bot しか参加していない状態でもループ再生を続けます。";
	private CommandType type = CommandType.EqualsIgnoreCase;
	private CommandPermission permission = CommandPermission.Everyone;
	
	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		Guild guild = event.getGuild();

		if (instance.getForceLoopMap().get(guild.getIdLong()) != null) {
			embedBuilder.setColor(Color.RED);
			embedBuilder.setDescription("🔁 強制ループ再生を無効にしました。");
			instance.getForceLoopMap().remove(guild.getIdLong());
			instance.getPreviousTrack().remove(guild.getIdLong());
		} else {
			embedBuilder.setColor(Color.GREEN);
			embedBuilder.setDescription("🔁 強制ループ再生を有効にしました。");
			instance.getForceLoopMap().put(guild.getIdLong(), instance.getPreviousTrack().get(guild.getIdLong()));
		}

		event.reply(embedBuilder.build());
	}

}
