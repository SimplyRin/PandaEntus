package net.simplyrin.pandaentus;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.besaba.revonline.pastebinapi.Pastebin;
import com.besaba.revonline.pastebinapi.impl.factory.PastebinFactory;
import com.besaba.revonline.pastebinapi.paste.PasteBuilder;
import com.besaba.revonline.pastebinapi.paste.PasteExpire;
import com.besaba.revonline.pastebinapi.paste.PasteVisiblity;
import com.google.common.io.Files;

import lombok.Data;
import lombok.Getter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.config.Config;
import net.simplyrin.pandaentus.listeners.Listener;
import net.simplyrin.pandaentus.listeners.MessageListener;
import net.simplyrin.pandaentus.listeners.ReactionListener;
import net.simplyrin.pandaentus.utils.GuildCallManager;
import net.simplyrin.pandaentus.utils.PoolItems;
import net.simplyrin.pandaentus.utils.TimeManager;
import net.simplyrin.pandaentus.utils.TimeUtils;
import net.simplyrin.pandaentus.utils.Version;
import net.simplyrin.processmanager.Callback;
import net.simplyrin.processmanager.ProcessManager;
import net.simplyrin.rinstream.RinStream;

/**
 * Created by SimplyRin on 2019/03/09.
 *
 * Copyright (C) 2019 SimplyRin
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
@Getter
public class Main {

	public static void main(String[] args) {
		new Main().run();
	}

	private Configuration config;
	private TimeManager timeManager;
	private PoolItems poolItems;
	private JDA jda;
	private TimeUtils timeUtils;
	private String voiceTextApiKey;
	private List<Message> messages = new ArrayList<>();

	public void run() {
		RinStream rinStream = new RinStream();
		rinStream.setSaveLog(true);
		System.out.println("setSavingLog: true");

		System.out.println("Loading files...");
		System.out.println("Build-Version: " + Version.BUILD_TIME);

		File file = new File("config.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Configuration config = Config.getConfig(file);
			config.set("Token", "BOT_TOKEN_HERE");
			config.set("Pastebin.API-Key", "PASTEBIN_API_KEY_HERE");
			config.set("VoiceTextApiKey", "VOICETEXT_API_KEY_HERE");
			config.set("Message-Type.Enable-Simple-Mode", false);

			Config.saveConfig(config, file);
		}

		this.config = Config.getConfig(file);
		this.timeManager = new TimeManager(this);

		this.poolItems = new PoolItems(this);

		this.timeUtils = new TimeUtils();

		this.voiceTextApiKey = this.config.getString("VoiceTextApiKey");

		JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);

		String token = this.config.getString("Token");
		if (token.equals("BOT_TOKEN_HERE")) {
			System.out.println("Discord Bot Token を config.yml に入力してください！");
			System.exit(0);
			return;
		}
		jdaBuilder.setToken(token);
		jdaBuilder.addEventListeners(new Listener(this));
		jdaBuilder.addEventListeners(new MessageListener(this));
		jdaBuilder.addEventListeners(new ReactionListener(this));

		this.jda = null;
		try {
			this.jda = jdaBuilder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// this.jda.getPresence().setActivity(Activity.playing("Build " + buildTime));
		// this.jda.getPresence().setActivity(Activity.playing("Source: github.com/SimplyRin/PandaEntus"));

		this.addShutdownHook(() -> {
			jda.shutdown();
			Config.saveConfig(Main.this.config, "config.yml");
			poolItems.save();
			System.out.println("Config ファイルを保存しました。");

			rinStream.close();
		});
	}

	public void addShutdownHook(Runnable runnable) {
		Runtime.getRuntime().addShutdownHook(new Thread(runnable));
	}

	@Getter
	private HashMap<String, GuildCallManager> guildCallMaps = new HashMap<>();

	public GuildCallManager getGuildCallManager(String channelId) {
		if (this.guildCallMaps.get(channelId) == null) {
			this.guildCallMaps.put(channelId, new GuildCallManager(this, channelId));
		}
		return this.guildCallMaps.get(channelId);
	}

	public String getNowTime() {
		Date date = new Date();
		return this.getNowTime(date);
	}

	public String getNowTime(Date date) {
		return date.getHours() + "時" + date.getMinutes() + "分";
	}

	public String getUptime(Date createdTime) {
		/**
		 * https://teratail.com/questions/28238
		 */
		Time time = this.getTimeFromDate(createdTime);

		String uptime = "";
		int year = time.getYear();
		int month = time.getMonth();
		int day = time.getDay();
		int hour = time.getHour();
		int minute = time.getMinute();
		int second = time.getSecond();

		if (year > 0) {
			uptime += year + "年";
		}
		if (month > 0) {
			uptime += month + "ヶ月";
		}
		if (day > 0) {
			uptime += day + "日 ";
		}

		if (hour > 0) {
			uptime += hour + "時間";
		}
		if (minute > 0) {
			uptime += minute + "分";
		}
		if (second > 0) {
			uptime += second + "秒";
		}

		if (uptime.trim().length() == 0) {
			uptime += "0秒";
		}

		return uptime;
	}

	public String getLTime(Time time) {

		String uptime = "";
		int hour = time.getHour();
		int minute = time.getMinute();
		int second = time.getSecond();

		if (hour > 0) {
			uptime += hour + "時間";
		}
		if (minute > 0) {
			uptime += minute + "分";
		}
		if (second > 0) {
			uptime += second + "秒";
		}

		if (uptime.trim().length() == 0) {
			uptime += "0秒";
		}

		return uptime;
	}

	public Time getTimeFromDate(Date createdTime) {
		int[] units = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
		Date today = new Date();
		int[] result = new int[units.length];

		Calendar sCal = Calendar.getInstance();
		Calendar tCal = Calendar.getInstance();

		sCal.setTime(createdTime);
		tCal.setTime(today);

		for (int i = units.length - 1; i >= 0; i--) {
			result[i] = tCal.get(units[i]) - sCal.get(units[i]);
			if (result[i] < 0) {
				tCal.add(units[i - 1], -1);
				int add = tCal.getActualMaximum(units[i]);
				result[i] += (units[i] == Calendar.DAY_OF_MONTH) ? add : add + 1;
			}
		}

		Time time = new Time();

		time.year = result[0];
		time.month = result[1];
		time.day = result[2];
		time.hour = result[3];
		time.minute = result[4];
		time.second = result[5];

		return time;
	}

	public static Time addTime(Time time1, Time time2) {
		int[] units = { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
		int[] result = new int[units.length];

		Calendar sCal = Calendar.getInstance();
		Calendar tCal = Calendar.getInstance();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date d1 = null;
		try {
			d1 = simpleDateFormat.parse(time1.year + "/" + time1.month + "/" + time1.day + " " + time1.hour + ":" + time1.minute + ":" + time1.second);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		sCal.setTime(d1);

		Date d2 = null;
		try {
			d2 = simpleDateFormat.parse(time2.year + "/" + time2.month + "/" + time2.day + " " + time2.hour + ":" + time2.minute + ":" + time2.second);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tCal.setTime(d2);

		for (int i = units.length - 1; i >= 0; i--) {
			result[i] = tCal.get(units[i]) - sCal.get(units[i]);
			if (result[i] < 0) {
				tCal.add(units[i - 1], -1);
				int add = tCal.getActualMaximum(units[i]);
				result[i] += (units[i] == Calendar.DAY_OF_MONTH) ? add : add + 1;
			}
		}

		Time time = new Time();

		time.year = result[0];
		time.month = result[1];
		time.day = result[2];
		time.hour = result[3];
		time.minute = result[4];
		time.second = result[5];

		return time;
	}

	/**
	 * https://qiita.com/sifue/items/07388fdada096734fa7f
	 */
	public void postError(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		String result = sw.toString();

		final PastebinFactory factory = new PastebinFactory();
		final Pastebin pastebin = factory.createPastebin(this.config.getString("Pastebin.API-Key"));
		final PasteBuilder pasteBuilder = factory.createPaste();

		// Title paste
		pasteBuilder.setTitle("Error message " + UUID.randomUUID().toString().split("-")[0]);
		pasteBuilder.setRaw(result);
		pasteBuilder.setMachineFriendlyLanguage("text");
		pasteBuilder.setVisiblity(PasteVisiblity.Unlisted);
		pasteBuilder.setExpire(PasteExpire.OneDay);

		String url = pastebin.post(pasteBuilder.build()).get();

		String localMessage;
		if (result.length() >= 1800) {
			localMessage = "```" +  result.substring(0, 1000) + "...```";
		} else {
			localMessage = "```" + result + "...```";
		}

		User user = this.jda.getUserById("224428706209202177");
		user.openPrivateChannel().complete().sendMessage("An error occured of PandaEntus discord bot!\r\nYou can visit error contents at " + url + localMessage).complete();
	}

	public static Date timeToDate(Time time) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = null;
		try {
			date = simpleDateFormat.parse(time.year + "/" + time.month + "/" + time.day + " " + time.hour + ":" + time.minute + ":" + time.second);
		} catch (ParseException e) {
		}
		return date;
	}

	public static Time dateToTime(Date date) {
		Time time = new Time();

		time.year = date.getYear();
		time.month = date.getMonth();
		time.day = date.getDay();
		time.hour = date.getHours();
		time.minute = date.getMinutes();
		time.second = date.getSeconds();

		return time;
	}

	@Data
	public static class Time {
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;

		public void addTime(Time time) {
			this.addTime(timeToDate(this));
		}

		public void addTime(Date d) {
			Time date = Main.addTime(this, dateToTime(d));

			this.hour = date.getHour();
			this.minute = date.getMinute();
			this.second = date.getSecond();
		}

		@Override
		public String toString() {
			String uptime = "";

			if (hour > 0) {
				uptime += hour + "時間";
			}
			if (minute > 0) {
				uptime += minute + "分";
			}
			if (second > 0) {
				uptime += second + "秒";
			}

			if (uptime.trim().length() == 0) {
				uptime += "0秒";
			}

			return uptime;
		}
	}

	public File downloadFile(String url) {
		final File file = new File("ytdl");
		file.mkdirs();

		final String videoId = this.getVideoId(url);

		final File mp3 = new File(file, videoId + ".mp3");
		if (mp3.exists()) {
			System.out.println("Cache found, " + mp3.getName());
			return mp3;
		}

		ProcessManager.runCommand(new String[]{ "youtube-dl", "--get-title", videoId }, new Callback() {
			@Override
			public void line(String response) {
				if (!response.toLowerCase().startsWith("error:")) {
					System.out.println("Title: " + response);
					config.set("YouTube." + videoId + ".Title", response);
				}
			}
		}, true);

		ProcessManager.runCommand(new String[]{ "youtube-dl", "--get-duration", videoId }, new Callback() {
			boolean ok = false;
			@Override
			public void line(String response) {
				if (response.contains(":")) {
					System.out.println("Duration: " + response);
					config.set("YouTube." + videoId + ".Duration", response);
				}
				int ll = response.split(":").length;
				if (ll == 2) {
					int length = Integer.valueOf(response.split(":")[0]);
					if (length <= 5) {
						ok = true;
					}
				}
			}

			@Override
			public void processEnded() {
				if (!ok) {
					System.out.println("Not Ready.");
					return;
				}

				ProcessManager.runCommand(new String[] { "youtube-dl", "--audio-format", "mp3", "-x", videoId }, new Callback() {
					private File file;
					@Override
					public void line(String response) {
						if (response.startsWith("[ffmpeg] Destination:")) {
							String title = response.replace("[ffmpeg] Destination:", "").replace("\"", "").trim();
							System.out.println("Filename: " + title);
							this.file = new File(title);
						}
					}

					@Override
					public void processEnded() {
						System.out.println("processEnded()");
						try {
							Files.move(this.file, mp3);
						} catch (IOException e) {
							e.printStackTrace();
						}

						config.set("YouTube." + videoId + ".Path", mp3.getAbsolutePath());
						System.out.println("P1");
					}
				}, false);
			}
		}, false);
		System.out.println("Returning value.");
		return mp3;
	}

	public String getVideoId(String url) {
		String tempVideoId = url.replace("https://www.youtube.com/watch?v=", "");
		if (tempVideoId.contains("&")) {
			tempVideoId = tempVideoId.split("&")[0];
		}
		tempVideoId = tempVideoId.replace("https://youtu.be/", "");
		tempVideoId = tempVideoId.replace("http://youtu.be/", "");
		tempVideoId = tempVideoId.replace("http://music.youtube.com/watch?v=", "");
		tempVideoId = tempVideoId.replace("http://www.youtube.com/watch?v=", "");
		return tempVideoId;
	}

}
