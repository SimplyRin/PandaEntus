package net.simplyrin.pandaentus.commands.general;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;

/**
 * Created by SimplyRin on 2020/12/11.
 *
 * Copyright (C) 2020 SimplyRin
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
public class AmazonUrlCommand implements BaseCommand {

	@Override
	public String getCommand() {
		return "https://www.amazon.co.jp/";
	}

	@Override
	public CommandType getType() {
		return CommandType.StartsWith;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, MessageReceivedEvent event, String[] args) {
		String url = args[0];
		if (url.length() <= 250) {
			return;
		}

		String result = null;
		if (url.contains("/dp/")) {
			String id = url.split("/dp/")[1].split("/")[0];
			result = "https://www.amazon.co.jp/dp/" + id;
		}

		if (result != null) {
			System.out.println(result);
		}
	}

	public static void main(String[] args) {
		AmazonUrlCommand auc = new AmazonUrlCommand();
		auc.execute(null, null, new String[] { "https://www.amazon.co.jp/Echo-Dot-%E3%82%A8%E3%82%B3%E3%83%BC%E3%83%89%E3%83%83%E3%83%88-%E7%AC%AC3%E4%B8%96%E4%BB%A3-with-Alexa-%E3%82%B9%E3%83%9E%E3%83%BC%E3%83%88%E3%82%B9%E3%83%94%E3%83%BC%E3%82%AB%E3%83%BC-%E3%83%98%E3%82%B6%E3%83%BC%E3%82%B0%E3%83%AC%E3%83%BC/dp/B07PFFMQ64/ref=sr_1_1?__mk_ja_JP=%E3%82%AB%E3%82%BF%E3%82%AB%E3%83%8A&dchild=1&keywords=Echo+Dot&qid=1607696162&sr=8-1" });
	}

}
