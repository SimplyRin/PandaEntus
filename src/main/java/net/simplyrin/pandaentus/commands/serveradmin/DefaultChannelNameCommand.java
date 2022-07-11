package net.simplyrin.pandaentus.commands.serveradmin;

import java.util.Arrays;
import java.util.List;

import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

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
public class DefaultChannelNameCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!defaultchannelname";
	}

	@Override
	public List<String> getAlias() {
		return Arrays.asList("!dcn");
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.ServerAdministrator;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		/* MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild();
		
		Configuration config = instance.getConfig(); */
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("set")) {
				if (args.length > 3) {
					var channelName = args[2];

					String defaultName = "";
					for (int i = 3; i < args.length; i++) {
						defaultName += args[i] + " ";
					}
					defaultName = defaultName.trim();
					
					instance.getConfig().set("DefaultChannelName." + channelName + ".Default", defaultName);
					instance.saveConfig();

					event.reply(channelName + " -> \"" + defaultName + "\"");
					return;
				}
				
				event.reply("使用方法: " + this.getCommand() + " set <チャンネルID> <デフォルトチャンネル名>");
				return;
			}
			
			if (args[1].equalsIgnoreCase("delete")) {
				if (args.length > 2) {
					instance.getConfig().set("DefaultChannelName." + args[2] + ".Default", null);
					
					event.reply("削除しました: " + args[2]);
					return;
				}
				
				event.reply("使用方法: " + this.getCommand() + " delete <チャンネルID>");
				return;
			}
		}
		
		event.reply("使用方法: " + this.getCommand() + " <set|delete> <チャンネルID> <チャンネル名>");
		return;
	}

}