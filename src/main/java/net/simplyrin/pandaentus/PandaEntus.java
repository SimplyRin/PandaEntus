package net.simplyrin.pandaentus;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.reflect.ClassPath;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.simplyrin.config.Config;
import net.simplyrin.config.Configuration;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.ReactionMessage;
import net.simplyrin.pandaentus.gamemanager.AkinatorManager;
import net.simplyrin.pandaentus.gamemanager.VoiceChannelNameManager;
import net.simplyrin.pandaentus.listeners.ActivityListener;
import net.simplyrin.pandaentus.listeners.CommandExecutor;
import net.simplyrin.pandaentus.listeners.Listener;
import net.simplyrin.pandaentus.listeners.ReactionListener;
import net.simplyrin.pandaentus.utils.PoolItems;
import net.simplyrin.pandaentus.utils.ThreadPool;
import net.simplyrin.pandaentus.utils.TimeManager;
import net.simplyrin.pandaentus.utils.TimeUtils;
import net.simplyrin.pandaentus.utils.Version;
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
public class PandaEntus {

	public static void main(String[] args) {
		new PandaEntus().run(args);
	}

	private Configuration config;
	private Configuration activityConfig;
	
	private TimeManager timeManager;
	private PoolItems poolItems;
	private JDA jda;
	private TimeUtils timeUtils;
	private String voiceTextApiKey;
	private List<ReactionMessage> messages = new ArrayList<>(); 

	private CommandExecutor commandRegister;

	private Map<Long, AudioTrack> loopMap;
	private Map<Long, AudioTrack> previousTrack;

	private AudioPlayerManager playerManager;
	private Map<Long, GuildMusicManager> musicManagers;
	private VoiceChannelNameManager vcNameManager;

	private Date startupDate = new Date();
	private Map<String, AkinatorManager> akiMap = new HashMap<>();
	
	@Setter
	private AudioPlaylist audioPlaylist;

	private List<String> amongUsGuildList;

	private Listener eventListener;
	private ActivityListener activityListener;
	
	private String botUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36";

	public void run(String[] args) {
		RinStream rinStream = new RinStream();
		rinStream.setPrefix("yyyy/MM/dd (E) HH:mm:ss")
				.enableError()
				.setSaveLog(true)
				.setEnableColor(true)
				.setEnableTranslateColor(true);
		
		if (Version.SHA.equals("")) {
			System.out.println("あなたは現在開発者ビルドを使用しています。");
			System.out.println("PandaEntus の公式ビルドは Jenkins からダウンロードすることができます。");
			System.out.println("Jenkins: https://ci.simplyrin.net");
			
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}

		if (args.length > 0 && args[0].equalsIgnoreCase("-tail")) {
			rinStream.tail();
			ThreadPool.run(() -> {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
				System.out.println("Tail enabled.");
			});
		}

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
			config.set("Admin-ID", "YOUR_ACCOUNT_ID_HERE");
			config.set("Pastebin.API-Key", "PASTEBIN_API_KEY_HERE");
			config.set("VoiceTextApiKey", "VOICETEXT_API_KEY_HERE");
			config.set("Message-Type.Enable-Simple-Mode", false);

			Config.saveConfig(config, file);
		}

		this.config = Config.getConfig(file);
		
		File activityData = new File("activity.yml");
		if (!activityData.exists()) {
			try {
				activityData.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.activityConfig = Config.getConfig(activityData);
		
		// パス再設定
		if (this.config.getString("Bot.Restart-Script", null) == null) {
			this.config.set("Bot.Restart-Script", "start.sh");
		}
		
		this.timeManager = new TimeManager(this);
		this.poolItems = new PoolItems(this);
		this.timeUtils = new TimeUtils();
		this.voiceTextApiKey = this.config.getString("VoiceTextApiKey");
		this.amongUsGuildList = new ArrayList<>();

		this.commandRegister = new CommandExecutor(this);

		String token = this.config.getString("Token");
		if (token.equals("BOT_TOKEN_HERE")) {
			System.out.println("Discord Bot Token を config.yml に入力してください！");
			System.exit(0);
			return;
		}
		try {
			List<GatewayIntent> list = new ArrayList<>();
			for (GatewayIntent intent : GatewayIntent.values()) {
				list.add(intent);
			}
			JDABuilder jdaBuilder = JDABuilder.createDefault(token, list);
			jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
			jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
			jdaBuilder.enableCache(CacheFlag.ACTIVITY);
			jdaBuilder.enableCache(CacheFlag.VOICE_STATE);
			this.jda = jdaBuilder.build().awaitReady();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.jda.addEventListener(this.commandRegister);
		this.jda.addEventListener(this.eventListener = new Listener(this));
		this.jda.addEventListener(new ReactionListener(this));
		this.jda.addEventListener(this.activityListener = new ActivityListener(this));

		// 自動登録
		try {
			System.out.println("コマンドを登録しています...");
			CommandListUpdateAction commands = this.getJda().updateCommands();
			
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			for (final ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClasses()) {
				if (classInfo.getName().startsWith("net.simplyrin.pandaentus.command")) {
					BaseCommand baseCommand = (BaseCommand) Class.forName(classInfo.getName()).getDeclaredConstructor().newInstance();
					this.commandRegister.registerCommand(commands, baseCommand.getCommand(), baseCommand);
				}
			}
			
			List<Command> commandList = commands.complete();
			System.out.println("コマンドを登録しました。スラッシュコマンド: " + commandList.size());
		} catch (Exception e) {
			System.out.println("エラーが発生しました。");
			e.printStackTrace();
			return;
		}

		try {
			System.out.println(this.jda.getSelfUser().getName());
			for (Guild guild : this.jda.getGuilds()) {
				System.out.println(guild.getName() + "@" + guild.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.loopMap = new HashMap<>();
		this.previousTrack = new HashMap<>();

		this.musicManagers = new HashMap<>();
		this.playerManager = new DefaultAudioPlayerManager();
		this.playerManager.createPlayer();
		AudioSourceManagers.registerRemoteSources(this.playerManager);
		AudioSourceManagers.registerLocalSource(this.playerManager);
		
		this.vcNameManager = new VoiceChannelNameManager(this);

		this.jda.getPresence().setActivity(Activity.playing("🐼"));

		this.addShutdownHook(() -> {
			jda.shutdown();
			Config.saveConfig(this.config, "config.yml");
			Config.saveConfig(this.activityConfig, "activity.yml");
			poolItems.save();
			System.out.println("Config ファイルを保存しました。");

			rinStream.close();
		});
	}
	
	public void saveConfig() {
		Config.saveConfig(this.config, "config.yml");
	}
	
	public void saveActivityConfig() {
		File activityData = new File("activity.yml");
		Config.saveConfig(this.activityConfig, activityData);
	}
	
	public Category getTextChannelCategory(Guild guild) {
		List<Category> list = guild.getCategoriesByName("Text Channels", true);
		if (list == null || list.isEmpty()) {
			list = guild.getCategoriesByName("テキストチャンネル", true);
		}
		
		return list.size() > 0 ? list.get(0) : null;
	}
	
	public Category getVoiceChannelCategory(Guild guild) {
		List<Category> list = guild.getCategoriesByName("Voice Channels", true);
		if (list == null || list.isEmpty()) {
			list = guild.getCategoriesByName("ボイスチャンネル", true);
		}
		
		return list.size() > 0 ? list.get(0) : null;
	}
	
	public String getVoiceChannelName(Category category) {
		String name = category.getName();
		if (name.equals("ボイスチャンネル")) {
			return "一般";
		} else {
			return "General";
		}
	}

	public void addShutdownHook(Runnable runnable) {
		Runtime.getRuntime().addShutdownHook(new Thread(runnable));
	}
	
	public int mmssToSeconds(String time) {
		int mm, ss;
		
		String[] split = time.contains(":") ? time.split(":") : time.split("：");
		mm = Integer.valueOf(split[0]);
		ss = Integer.valueOf(split[1]);
		
		return (mm * 60) + ss;
	}

	public String getNowTime() {
		Date date = new Date();
		return this.getNowTime(date);
	}

	@SuppressWarnings("deprecation")
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

		File file = this.stringToTempFile(result);
		
		for (String id : this.getBotOwner()) {
			User user = this.jda.getUserById(id);
			user.openPrivateChannel().complete().sendFiles(FileUpload.fromData(file)).complete();
		}
	}
	
	public File stringToTempFile(String message) {
		try {
			File folder = new File("temp");
			folder.mkdirs();
			
			File file = new File(folder, UUID.randomUUID().toString().split("-")[0] + ".txt");
			
			Files.writeString(Path.of(file.toURI()), message, StandardCharsets.UTF_8);
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
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

	@SuppressWarnings("deprecation")
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
			Time date = PandaEntus.addTime(this, dateToTime(d));

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

	public List<String> getBotOwner() {
		return this.getConfig().getStringList("BotOwnerList");
	}

	private boolean notice;
	
	public boolean isBotOwner(User user) {
		boolean bool = false;
		
		List<String> botOwnerList = this.getBotOwner();
		if (botOwnerList == null || botOwnerList.isEmpty()) {
			this.getConfig().set("BotOwnerList", Arrays.asList("999", "888"));
			if (!this.notice) {
				System.out.println("Admin-ID が設定されていません。Config ファイルから Admin-ID を設定してください。");
				this.notice = true;
			}
		} else {
			for (String id : botOwnerList) {
				if (user.getId().equals(id)) {
					bool = true;
				}
			}
		}
		
		return bool;
	}

	public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
		long guildId = guild.getIdLong();
		GuildMusicManager musicManager = this.musicManagers.get(guildId);

		if (musicManager == null) {
			musicManager = new GuildMusicManager(this, guild, this.playerManager);
			this.musicManagers.put(guildId, musicManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		return musicManager;
	}

	public void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
		int volume = this.config.getInt("Guild." + guild.getId() + ".Voice-Volume", 1);
		musicManager.getPlayer().setVolume(volume);
		musicManager.getScheduler().queue(track);
	}

	public void skipTrack(Guild guild, MessageChannel channel) {
		GuildMusicManager musicManager = this.getGuildAudioPlayer(guild);
		musicManager.getScheduler().nextTrack();

		// channel.sendMessage("次の曲にスキップします。").queue();
	}

	public int durationToTime(String duration) {
		int i = 0;

		try {
			if (duration.contains(":")) {
				int min = Integer.valueOf(duration.split(":")[0]);
				int sec = Integer.valueOf(duration.split(":")[1]);

				i += min * 60;
				i += sec;
			} else {
				i += Integer.valueOf(duration);
			}
		} catch (Exception e) {
		}

		return i;
	}
	
	/**
	 * https://stackoverflow.com/questions/9214786/how-to-convert-the-seconds-in-this-format-hhmmss/47841277
	 */
	public String formatMillis(long millis) {
		long secs = millis / 1000;
		return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
	}

	public String formatSize(double size) {
		String hrSize = null;

		double b = size;
		double k = size / 1024.0;
		double m = k / 1024.0;
		double g = m / 1024.0;
		double t = g / 1024.0;

		DecimalFormat decimalFormat = new DecimalFormat("0.00");

		if (t > 1) {
			hrSize = decimalFormat.format(t).concat(" TB");
		} else if (g > 1) {
			hrSize = decimalFormat.format(g).concat(" GB");
		} else if (m > 1) {
			hrSize = decimalFormat.format(m).concat(" MB");
		} else if (k > 1) {
			hrSize = decimalFormat.format(k).concat(" KB");
		} else {
			hrSize = decimalFormat.format(b).concat(" Bytes");
		}

		return hrSize;
	}


}
