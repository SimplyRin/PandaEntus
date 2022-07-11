package net.simplyrin.pandaentus.commands.botowner;

import java.io.File;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;

import net.dv8tion.jda.api.entities.Message;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.simplyrin.pandaentus.utils.Version;

/**
 * Created by SimplyRin on 2021/11/21.
 *
 * Copyright (C) 2021 SimplyRin
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
public class UpdateCommand extends BaseCommand {
	
	private String jenkins = "https://ci.simplyrin.net";

	@Override
	public String getCommand() {
		return "!update";
	}

	@Override
	public String getDescription() {
		return "PandaEntus のアップデート確認し、最新バージョンにアップデートします。";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.BotOwner;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		try {
			var buildNumber = this.getStableBuildNumber();
			
			if (Version.JENKINS_BUILDER_NUMBER.equalsIgnoreCase(buildNumber)) {
				event.reply("最新の PandaEntus 🐼 を利用しています。\nhttps://ci.simplyrin.net/job/PandaEntus/" + Version.JENKINS_BUILDER_NUMBER + "/");
			} else {
				event.reply("アップデートを確認しました。PandaEntus 🐼 を更新しています...。\n"
						+ "現在のバージョン: **" + Version.JENKINS_BUILDER_NUMBER + "**\n"
						+ "最新のバージョン: **" + buildNumber + "**");

				File file = new File("PandaEntus-1.3-jar-with-dependencies-v" + buildNumber + ".jar");
				
				Message message = event.reply("ファイルをダウンロードしています...。");
				this.downloadJar(file);
				message.editMessage("ファイルをダウンロードしました。").complete();
				
				File now = new File("PandaEntus-1.3-jar-with-dependencies.jar");
				if (now.delete()) {
					file.renameTo(now);
					message.editMessage("PandaEntus を更新しました。再起動してください。").complete();
				} else {
					message.editMessage("更新に失敗しました。Bot を停止して " + file.getName() + " を " + now.getName() + " に置き換えて使用してください。").complete();
				}
			}
			
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		event.reply("アップデートの確認に失敗しました。");
	}
	
	public boolean downloadJar(File file) {
		String url = this.jenkins + "/job/PandaEntus/lastSuccessfulBuild/artifact/target/PandaEntus-1.3-jar-with-dependencies.jar";
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
			connection.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36");
			connection.connect();
			FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getStableBuildNumber() {
		String url = this.jenkins + "/job/PandaEntus/lastStableBuild/buildNumber";
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
			connection.addRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.150 Safari/537.36");
			connection.connect();
			Scanner scanner = new Scanner(connection.getInputStream());
			String buildNumber = scanner.nextLine();
			scanner.close();
			return buildNumber;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
