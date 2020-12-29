package net.simplyrin.pandaentus.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.simplyrin.pandaentus.Main;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.Permission;

/**
 * Created by SimplyRin on 2020/12/11.
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
	public Permission getPermission() {
		return Permission.Everyone;
	}

	@Override
	public void execute(Main instance, MessageReceivedEvent event, String[] args) {
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
