package net.simplyrin.pandaentus.gamemanager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.utils.ThreadPool;
import org.eu.zajc.akiwrapper.Akiwrapper;
import org.eu.zajc.akiwrapper.AkiwrapperBuilder;
import org.eu.zajc.akiwrapper.core.entities.Guess;
import org.eu.zajc.akiwrapper.core.entities.Query;
import org.eu.zajc.akiwrapper.core.entities.Question;

/**
 * Created by SimplyRin on 2020/03/18.
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
@Getter
public class AkinatorManager extends ListenerAdapter {

	private Akiwrapper akiWrapper;
	private Guild guild;
	private String channelId;

	@Setter
	private String LatestMessageId;

	private String lastChannel;

	private boolean isGameEnd = false;

	private Query query;

	private boolean isReceivingQuestion;
	private Question question;

	private Guess guess;

	public AkinatorManager(Guild guild, String channelId) {
		this.guild = guild;
		this.channelId = channelId;

		this.akiWrapper = new AkiwrapperBuilder()
				.setFilterProfanity(false)
				.setLanguage(Akiwrapper.Language.JAPANESE)
				.build();

		guild.getJDA().addEventListener(this);

		TextChannel channel = guild.getTextChannelById(this.channelId);

		this.query = this.akiWrapper.getCurrentQuery();

		while (this.query != null) {
			if (this.isGameEnd) {
				break;
			}

			EmbedBuilder embedBuilder = new EmbedBuilder();

			if (this.query instanceof Question question) {
				// onMessageReceived を有効にして、結果を送る

				this.isReceivingQuestion = true;
				this.question = question;

				embedBuilder = this.setEmbed(this.question);

				channel.sendMessageEmbeds(embedBuilder.build()).complete();

				while (this.isReceivingQuestion) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }


			}

			else if (this.query instanceof Guess guess) {
				this.guess = guess;

				embedBuilder.setAuthor("あなたが想像しているのは...");
				embedBuilder.setDescription(guess.getName() + " (" + guess.getDescription() + ")");
				embedBuilder.setImage(guess.getImage().toExternalForm());
				embedBuilder.addField("正解！", "1", true);
				embedBuilder.addField("違うので続ける...", "2", true);

				this.isReceivingQuestion = true;
				this.checkEnd = true;
				channel.sendMessageEmbeds(embedBuilder.build()).complete();

				if (guess.getDescription() != null && !guess.getDescription().equals("---")) {
					System.out.println(guess.getDescription());
				}

				while (this.checkEnd) {
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		System.out.println("Bravo!");
	}

	private boolean checkEnd = false;

	public Akiwrapper.Answer parseAnswer(String value) {
		TextChannel textChannel = this.guild.getTextChannelById(this.channelId);

		if (this.checkEnd) {
			switch (value.toLowerCase()) {
			case "1":
			case "１":
				textChannel.sendMessage("おち").complete();

				this.isGameEnd = true;

				this.isReceivingQuestion = false;
				this.checkEnd = false;
				this.guess.confirm();

				this.akiWrapper = null;
				this.guild.getJDA().removeEventListener(this);
				return null;
			case "2":
			case "２":
				this.isReceivingQuestion = false;
				this.checkEnd = false;

				this.query = this.guess.reject();
			}

			return null;
		}


		switch (value.toLowerCase()) {
		case "はい":
		case "1":
		case "１":
			return Akiwrapper.Answer.YES;
		case "いいえ":
		case "2":
		case "２":
			return Akiwrapper.Answer.NO;
		case "わからない":
		case "分からない":
		case "3":
		case "３":
			return Akiwrapper.Answer.DONT_KNOW;
		case "たぶんそう":
		case "部分的にそう":
		case "4":
		case "４":
			return Akiwrapper.Answer.PROBABLY;
		case "たぶん違う":
		case "そうでもない":
		case "5":
		case "５":
			return Akiwrapper.Answer.PROBABLY_NOT;
		case "もどる":
		case "戻る":
		case "7":
		case "７":
			textChannel.sendTyping().complete();

			this.query = this.question.undoAnswer();

			return null;
		case "やめる":
		case "8":
		case "８":
			textChannel.sendMessage(":pray:").complete();

			this.isGameEnd = true;

			this.akiWrapper = null;
			this.guild.getJDA().removeEventListener(this);
			return null;
		}
		return null;
	}

	public EmbedBuilder setEmbed(Question question) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setDescription("**" + question.getText() + "**");
		embedBuilder.setAuthor("進行度: " + question.getProgression() + "% (" + (question.getStep() + 1) + ")");
		embedBuilder.addField("はい", "1", true);
		embedBuilder.addField("いいえ", "2", true);
		embedBuilder.addField("わからない", "3", true);
		embedBuilder.addField("たぶんそう", "4", true);
		embedBuilder.addField("たぶんちがう", "5", true);
		embedBuilder.addField("もどる", "7", true);
		embedBuilder.addField("やめる", "8", true);
		return embedBuilder;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return;
		}

		if (event.getAuthor().isBot()) {
			return;
		}

		Guild guild = event.getGuild();
		MessageChannel channel = event.getChannel();

		if (!guild.getId().equals(this.guild.getId())) {
			return;
		}

		if (!channel.getId().equals(this.channelId)) {
			return;
		}

		if (!this.isReceivingQuestion) {
			return;
		}

		String message = event.getMessage().getContentRaw().trim();

		ThreadPool.run(() -> {
			Akiwrapper.Answer answer = this.parseAnswer(message);

			if (answer != null) {
				channel.sendTyping().complete();

				this.query = this.question.answer(answer);
			}

			this.isReceivingQuestion = false;
		});
	}

}
