package net.simplyrin.pandaentus.gamemanager.wordwolf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.commands.minigame.WordWolfCommand;
import net.simplyrin.pandaentus.utils.ThreadPool;

/**
 * Created by SimplyRin on 2021/11/09.
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
@Getter
public class WordWolfManager extends ListenerAdapter {
	
	private PandaEntus instance;
	private Guild guild;
	private MessageChannel channel;
	private int seconds;
	
	// 人数、狼の数、時間
	private int playerCount;
	private int wolfs;
	
	private boolean init;
	private String gameId;
	
	private boolean gameStarted = false; 
	private boolean gameEnd = false;
	
	// 参加者の Discord ID
	private List<String> players = new ArrayList<>();
	
	// リアクションメッセージ
	private Message recruitMessage;
	
	// 多数派お題
	private String playerTheme;
	// 少数派お題
	private String wolfTheme;
	
	// 市民
	private List<String> generalPlayers = new ArrayList<>();
	// 狼
	private List<String> wolfPlayers = new ArrayList<>();
	
	// 投票用
	private HashMap<Integer, Member> voteMap = new HashMap<>();
	// 投票受付状態
	private boolean voteReception;
	// 投票されたマップ
	private HashMap<Member, Integer> votedMap = new HashMap<>();
	// 投票数
	private int votedCount = 0;
	// 投票が必要なユーザー
	private List<String> voteNeededPlayers = new ArrayList<>();
	
	private static HashMap<String, WordWolfManager> games = new HashMap<>();
	
	public WordWolfManager(PandaEntus instance, Guild guild, MessageChannel channel, int wolfs, int seconds, String theme) {
		this.instance = instance;
		this.guild = guild;
		this.channel = channel;
		this.wolfs = wolfs;
		this.seconds = seconds;

		// テーマファイル特定
		WordWolfTheme wwt = null;
		if (theme == null) {
			wwt = WordWolfTheme.getTheme();
		} else {
			wwt = WordWolfTheme.getTheme(theme);
		}
		this.playerTheme = wwt.getA();
		this.wolfTheme = wwt.getB();
		
		this.gameId = UUID.randomUUID().toString().split("-")[0];

		instance.getJda().addEventListener(this);
		
		games.put(this.gameId, this);
	}
	
	public WordWolfManager startRecruit() {
		this.recruitMessage = this.channel.sendMessage("ワードウルフのプレイヤー募集をします。\n"
				+ "参加したいユーザーはリアクション 👌 を押して待機してください。 (" + this.gameId + ")").complete();
		this.recruitMessage.addReaction("👌").complete();

		return this;
	}
	
	public WordWolfManager start() {
		if (this.gameStarted) {
			return null;
		}
		
		if (this.players.size() <= 1) {
			return this;
		}
		
		this.gameStarted = true;
		
		// ウルフ
		int wolf = this.wolfs;
		List<String> ids = new ArrayList<>();
		ids.addAll(this.players);
		Collections.shuffle(ids);
		
		while (wolf >= 1) {
			wolf--;

			this.wolfPlayers.add(ids.get(0));
			ids.remove(0);
			
		}
		this.generalPlayers.addAll(ids);

		for (String wolfP : this.wolfPlayers) {
			Member member = this.guild.getMemberById(wolfP);
			member.getUser().openPrivateChannel().complete().sendMessage("ゲームが始まりました。お題: **" + this.wolfTheme + "**").complete();
		}
		
		for (String generalP : this.generalPlayers) {
			Member member = this.guild.getMemberById(generalP);
			member.getUser().openPrivateChannel().complete().sendMessage("ゲームが始まりました。お題: **" + this.playerTheme + "**").complete();
		}
		
		this.channel.sendMessage("個人チャットへお題を送信しました。\nゲームを開始しました。お題に従って話し合いを始めてください。").complete();
		
		Message message = this.channel.sendMessage("残り時間: " + this.instance.formatMillis(this.seconds * 1000)).complete();
		
		ThreadPool.run(() -> {
			while (this.seconds >= 0) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
				}
				this.seconds--;
			}
			
			// ゲーム終了
			this.gameEnd = true;
		});
		
		ThreadPool.run(() -> {
			while (true) {
				try {
					TimeUnit.SECONDS.sleep(4);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (this.gameEnd) {
					message.editMessage("残り時間: 0:00").complete();
					this.channel.sendMessage("ゲームが終了しました。\n個人チャットへ送信したメッセージに従ってください。").complete();
					this.voteReception = true;
					break;
				} else {
					ThreadPool.run(() -> message.editMessage("残り時間: " + this.instance.formatMillis(this.seconds * 1000)).complete());
				}
			}
			
			String members = "";
			
			int i = 1;
			for (String id : this.players) {
				Member member = this.guild.getMemberById(id);
				this.voteMap.put(i, member);
				
				members += i + ": " + member.getEffectiveName() + "\n";
				i++;
				
				this.voteNeededPlayers.add(id);
			}
			
			for (String id : this.players) {
				Member member = this.guild.getMemberById(id);
				member.getUser().openPrivateChannel().complete().sendMessage("狼だと思う人の名前または、最初の番号を入力してください。\n```" + members + "```").complete();
			}
			
			ThreadPool.run(() -> {
				try {
					TimeUnit.SECONDS.sleep(60);
				} catch (Exception e) {
				}
				
				// TODO まだ投票が終了していない場合強制的にゲームを終了する
				if (this.voteNeededPlayers.size() >= 1) {
					this.channel.sendMessage("下記のプレイヤーが投票を行いませんでしたが、時間制限のためゲームを終了しました。").complete();
					
					this.showResult();
				}
			});
		});
		
		return this;
	}
	
	// 投票内容受付 -> 全員が終わったら結果を表示してゲーム終了
	
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		if (!this.voteReception) {
			return;
		}
		
		if (event.getAuthor().isBot()) {
			return;
		}
		
		if (!this.voteNeededPlayers.contains(event.getAuthor().getId())) {
			event.getChannel().sendMessage("あなたは既に投票済みです！").complete();
			return;
		}
		
		String message = event.getMessage().getContentRaw();
		int msgId = 999;
		try {
			msgId = Integer.valueOf(message);
		} catch (Exception e) {
		}
		
		// 投票ユーザー
		Member member = null;
		
		for (Entry<Integer, Member> entry : this.voteMap.entrySet()) {
			int id = entry.getKey();
			Member m = entry.getValue();
			
			if (id == msgId) {
				member = m;
			}
			
			if (m.getEffectiveName().equalsIgnoreCase(message)) {
				member = m;
			}
		}
		
		if (member != null) {
			if (this.votedMap.get(member) != null) {
				int i =this.votedMap.get(member);
				this.votedMap.put(member, i + 1);
			} else {
				this.votedMap.put(member, 1);
			}
			this.votedCount++;
			event.getChannel().sendMessage(member.getEffectiveName() + " に投票しました。").complete();
			this.voteNeededPlayers.remove(event.getAuthor().getId());
			
			// 投票終了
			if (this.votedCount == this.players.size()) {
				this.showResult();
			}
		}
	}
	
	public WordWolfManager showResult() {
		String msg = "";
		
		// 人狼の投票率が一番多い場合
		/* int max;
		Member topVoted;
		for (Entry<Member, Integer> entry : this.votedMap.entrySet()) {
			max = 
		} */
		
		msg += "人狼:\n";
		for (String id : this.wolfPlayers) {
			try {
				Member member = this.guild.getMemberById(id);
				msg += this.instance.getNickname(member) + " (投票数: " + this.getVotedCount(member) + ")\n";
			} catch (Exception e) {
			}
		}
		
		msg += "\n市民:\n";
		for (String id : this.generalPlayers) {
			try {
				Member member = this.guild.getMemberById(id);
				msg += this.instance.getNickname(member) + " (投票数: " + this.getVotedCount(member) + ")\n";
			} catch (Exception e) {
			}
		}
		
		WordWolfCommand wwc = (WordWolfCommand) this.instance.getCommandRegister().getRegisteredCommand(WordWolfCommand.class);
		this.channel.sendMessage("ゲームが終了しました。結果:\n```" + msg + "```\nもう一度始める場合は **" + wwc.getCommand() + "** を使用してください。").complete();
		
		this.voteReception = false;
		
		this.instance.getJda().removeEventListener(this);
		games.remove(this.gameId);
		return this;
	}
	
	public int getVotedCount(Member member) {
		int count = 0;
		for (Entry<Member, Integer> entry : this.votedMap.entrySet()) {
			if (entry.getKey().getId().equals(member.getId())) {
				count = entry.getValue();
			}
		}
		return count;
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser().isBot()) {
			return;
		}
		
		if (this.recruitMessage == null && !this.recruitMessage.getId().equals(event.getMessageId())) {
			return;
		}
		
		WordWolfCommand wwc = (WordWolfCommand) this.instance.getCommandRegister().getRegisteredCommand(WordWolfCommand.class);
		
		Member member = event.getMember();
		
		this.players.add(member.getId());

		PrivateChannel channel = member.getUser().openPrivateChannel().complete();
		channel.sendMessage("ワードウルフへの参加を受け付けました。\n"
				+ "ゲーム開始までお待ちください。\n"
				+ "開始コマンド: **" + wwc.getCommand() + " start**").complete();
		
	}
	
	public static WordWolfManager getGame(String gameId) {
		return games.get(gameId);
	}
	
	public static WordWolfManager getGameByChannel(MessageChannel channel) {
		WordWolfManager wwm = null;
		for (WordWolfManager value : games.values()) {
			if (value.channel.getId().equals(channel.getId())) {
				wwm = value;
			}
		}
		return wwm;
	}

}
