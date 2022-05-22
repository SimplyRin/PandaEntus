package net.simplyrin.pandaentus.commands.botowner;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
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
public class UpdateCommand implements BaseCommand {
	
	private String url = "https://ci.simplyrin.net";

	@Override
	public String getCommand() {
		return "!update";
	}

	@Override
	public String getDescription() {
		return "PandaEntus のアップデート確認し、最新バージョンにアップデートします。";
	}

	@Override
	public CommandData getCommandData() {
		return null;
	}

	@Override
	public List<String> getAlias() {
		return null;
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
		String commitUrl = "https://github.com/SimplyRin/PandaEntus/commit/";
		
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
			connection.addRequestProperty("user-agent", instance.getBotUserAgent());
			
			String result = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
			JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
			
			String latestSha = jsonObject.get("sha").getAsString().substring(0, 7);
			String currentSha = Version.SHA.length() == 0 ? "Development" : Version.SHA;
			
			var buildNumber = this.getStableBuildNumber();
			
			if (Version.JENKINS_BUILDER_NUMBER.equalsIgnoreCase(buildNumber)) {
				event.reply("最新の PandaEntus 🐼 を利用しています。\n" + commitUrl + currentSha);
			} else {
				event.reply("アップデートを確認しました。PandaEntus 🐼 を更新しています...。\n"
						+ "現在のバージョン: **" + currentSha + "** ( " + commitUrl + currentSha + " )\n"
						+ "最新のバージョン: **" + latestSha + "** ( " + commitUrl + latestSha + " )");

				File file = new File(latestSha + "-v" + buildNumber + ".jar");
				
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
		String url = this.url + "/job/PandaEntus/lastSuccessfulBuild/artifact/target/PandaEntus-1.3-jar-with-dependencies.jar";
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
		String url = this.url + "/job/PandaEntus/lastStableBuild/buildNumber";
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
