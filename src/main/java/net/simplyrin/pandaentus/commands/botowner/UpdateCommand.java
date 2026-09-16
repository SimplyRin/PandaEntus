package net.simplyrin.pandaentus.commands.botowner;

import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

	private static final String REPOSITORY = "SimplyRin/PandaEntus";
	private static final String RELEASES_URL = "https://github.com/" + REPOSITORY + "/releases";
	private static final String RELEASES_API = "https://api.github.com/repos/" + REPOSITORY + "/releases?per_page=10";
	private static final String USER_AGENT = "PandaEntus-Updater";

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
			if (Version.GITHUB_RUN_NUMBER.isEmpty()) {
				event.reply("開発者ビルドを使用しているため、アップデートを確認できません。\n" + RELEASES_URL);
				return;
			}

			JsonObject release = this.getLatestRelease();
			if (release == null) {
				event.reply("アップデートの確認に失敗しました。");
				return;
			}

			String runNumber = this.getRunNumber(release);
			String releaseUrl = release.get("html_url").getAsString();

			if (Version.GITHUB_RUN_NUMBER.equalsIgnoreCase(runNumber)) {
				event.reply("最新の PandaEntus 🐼 を利用しています。\n" + releaseUrl);
				return;
			}

			String jarName = this.getJarName();
			String downloadUrl = this.getAssetUrl(release, jarName);
			if (downloadUrl == null) {
				event.reply("最新のリリースに " + jarName + " が添付されていませんでした。\n" + releaseUrl);
				return;
			}

			event.reply("アップデートを確認しました。PandaEntus 🐼 を更新しています...。\n"
					+ "現在のバージョン: **" + Version.GITHUB_RUN_NUMBER + "**\n"
					+ "最新のバージョン: **" + runNumber + "**\n"
					+ releaseUrl);

			File file = new File("PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies-v" + runNumber + ".jar");

			Message message = event.reply("ファイルをダウンロードしています...。");
			if (!this.downloadJar(downloadUrl, file)) {
				message.editMessage("ファイルのダウンロードに失敗しました。").complete();
				return;
			}
			message.editMessage("ファイルをダウンロードしました。").complete();

			File now = new File(jarName);
			if (now.delete()) {
				file.renameTo(now);
				message.editMessage("PandaEntus を更新しました。再起動してください。").complete();
			} else {
				message.editMessage("更新に失敗しました。Bot を停止して " + file.getName() + " を " + now.getName() + " に置き換えて使用してください。").complete();
			}

			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		event.reply("アップデートの確認に失敗しました。");
	}

	public String getJarName() {
		return "PandaEntus-" + Version.POMVERSION + "-jar-with-dependencies.jar";
	}

	/**
	 * Pre-release も含めた最新のリリースを返します。draft は対象外です。
	 */
	public JsonObject getLatestRelease() {
		try {
			HttpURLConnection connection = (HttpURLConnection) URI.create(RELEASES_API).toURL().openConnection();
			connection.addRequestProperty("user-agent", USER_AGENT);
			connection.addRequestProperty("accept", "application/vnd.github+json");
			connection.connect();

			JsonArray releases;
			try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
				releases = JsonParser.parseReader(reader).getAsJsonArray();
			}

			// リリース一覧は作成日時の新しい順に返るため、draft でない最初の要素が最新。
			for (JsonElement element : releases) {
				JsonObject release = element.getAsJsonObject();
				if (!release.get("draft").getAsBoolean()) {
					return release;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * タグ名 (v123) から GitHub Actions の run number (123) を取り出します。
	 */
	public String getRunNumber(JsonObject release) {
		String tagName = release.get("tag_name").getAsString();
		return tagName.startsWith("v") ? tagName.substring(1) : tagName;
	}

	public String getAssetUrl(JsonObject release, String assetName) {
		for (JsonElement element : release.getAsJsonArray("assets")) {
			JsonObject asset = element.getAsJsonObject();
			if (asset.get("name").getAsString().equals(assetName)) {
				return asset.get("browser_download_url").getAsString();
			}
		}
		return null;
	}

	public boolean downloadJar(String url, File file) {
		try {
			HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
			connection.addRequestProperty("user-agent", USER_AGENT);
			// browser_download_url は objects.githubusercontent.com へリダイレクトされる。
			connection.setInstanceFollowRedirects(true);
			connection.connect();
			FileUtils.copyInputStreamToFile(connection.getInputStream(), file);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
