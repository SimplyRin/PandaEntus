package net.simplyrin.pandaentus;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.md_5.bungee.config.Configuration;
import net.simplyrin.config.Config;
import net.simplyrin.pandaentus.listeners.Listener;
import net.simplyrin.pandaentus.listeners.MessageListener;
import net.simplyrin.rinstream.RinStream;

/**
 * Created by SimplyRin on 2019/03/09.
 */
public class Main {

	public static void main(String[] args) {
		new Main().run();
	}

	@Getter
	private Configuration config;

	@Getter
	private JDA jda;

	public void run() {
		new RinStream();

		System.out.println("Loading files...");

		File file = new File("config.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Configuration config = Config.getConfig(file);
			config.set("Token", "BOT_TOKEN_HERE");

			Config.saveConfig(config, file);
		}

		this.config = Config.getConfig(file);

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

		this.jda = null;
		try {
			this.jda = jdaBuilder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				jda.shutdown();
				Config.saveConfig(Main.this.config, "config.yml");
				System.out.println("Config ファイルを保存しました。");
			}
		});
	}

	public String getUptime(Date createdTime) {
		/**
		 * https://teratail.com/questions/28238
		 */
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

		String uptime = "";
		int year = result[0];
		int month = result[1];
		int day = result[2];
		int hour = result[3];
		int minute = result[4];
		int second = result[5];

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

		return uptime;
	}

}
