package net.simplyrin.pandaentus.commands.admin;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;

/**
 * Created by SimplyRin on 2020/07/09.
 *
 * Copyright (c) 2020 SimplyRin
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
public class InitCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!init";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public Permission getPermission() {
		return Permission.Administrator;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();

		if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
			Category textCategory = guild.createCategory("Text Channels").complete();
			TextChannel textChannel = textCategory.createTextChannel("general-1").complete();
			Category voiceCategory = guild.createCategory("Voice Channels").complete();
			voiceCategory.createVoiceChannel("General-1").setUserlimit(99).complete();
			textChannel.sendMessage("Bot で必要なカテゴリ、チャンネルを自動作成しました。").complete();
			return;
		}

		channel.sendMessage("!init confirm と入力すると、Bot で必要なカテゴリ、チャンネルを自動的に作成します。\n"
				+ "作成されるチャンネルは以下のとおりです。\nカテゴリ: `Text Channels`, `Voice Channels`"
				+ "\nテキストチャンネル: `#general`"
				+ "\nボイスチャンネル: `General-1`").complete();
	}

}
