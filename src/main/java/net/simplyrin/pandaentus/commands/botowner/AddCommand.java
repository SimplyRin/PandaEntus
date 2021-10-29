package net.simplyrin.pandaentus.commands.botowner;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;

/**
 * Created by SimplyRin on 2020/10/20.
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
public class AddCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "!add";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public Permission getPermission() {
		return Permission.BotOwner;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
		MessageChannel channel = event.getChannel();

		if (args.length > 3) {
			Configuration config = instance.getConfig();

			config.set("LocalMessage." + channel.getId() + ".Message", args[1]);
			config.set("LocalMessage." + channel.getId() + ".Value.1", args[2]);
			config.set("LocalMessage." + channel.getId() + ".Value.2", args[3]);

			channel.sendMessage("設定しました。").complete();
			return;
		}

		channel.sendMessage("使用方法: !add <メッセージ> <内容1> <内容2>").complete();
		return;
	}

}
