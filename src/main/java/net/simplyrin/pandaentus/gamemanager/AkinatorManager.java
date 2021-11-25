package net.simplyrin.pandaentus.gamemanager;

import java.util.List;

import com.markozajc.akiwrapper.Akiwrapper;
import com.markozajc.akiwrapper.Akiwrapper.Answer;
import com.markozajc.akiwrapper.AkiwrapperBuilder;
import com.markozajc.akiwrapper.core.entities.Guess;
import com.markozajc.akiwrapper.core.entities.Question;
import com.markozajc.akiwrapper.core.entities.Server.Language;
import com.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.simplyrin.pandaentus.utils.ThreadPool;

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

	public AkinatorManager(Guild guild, String channelId) {
		this.guild = guild;
		this.channelId = channelId;

		try {
			this.akiWrapper = new AkiwrapperBuilder()
					.setFilterProfanity(false)
					.setLanguage(Language.JAPANESE)
					.build();
		} catch (ServerNotFoundException e) {
			e.printStackTrace();
		}

		guild.getJDA().addEventListener(this);
	}

	private boolean checkEnd = false;

	public Answer parseAnswer(String value) {
		TextChannel textChannel = this.guild.getTextChannelById(this.channelId);

		if (this.checkEnd) {
			switch (value.toLowerCase()) {
			case "1":
			case "１":
				textChannel.sendMessage("お疲れさまでした。").complete();

				this.akiWrapper = null;
				this.guild.getJDA().removeEventListener(this);
				return null;
			case "2":
			case "２":
				this.checkEnd = false;
			}

			
			textChannel.sendTyping().complete();

			EmbedBuilder embedBuilder = new EmbedBuilder();
			this.setEmbed(embedBuilder);

			// textChannel.sendMessage(embedBuilder.build()).complete();

			return null;
		}


		switch (value.toLowerCase()) {
		case "はい":
		case "1":
		case "１":
			return Answer.YES;
		case "いいえ":
		case "2":
		case "２":
			return Answer.NO;
		case "わからない":
		case "分からない":
		case "3":
		case "３":
			return Answer.DONT_KNOW;
		case "たぶんそう":
		case "部分的にそう":
		case "4":
		case "４":
			return Answer.PROBABLY;
		case "たぶん違う":
		case "そうでもない":
		case "5":
		case "５":
			return Answer.PROBABLY_NOT;
		case "もどる":
		case "戻る":
		case "7":
		case "７":
			textChannel.sendTyping().complete();

			this.akiWrapper.undoAnswer();

			textChannel.sendMessageEmbeds(this.setEmbed(null).build()).complete();
			return null;
		case "やめる":
		case "8":
		case "８":
			textChannel.sendMessage(":pray:").complete();

			this.akiWrapper = null;
			this.guild.getJDA().removeEventListener(this);
			return null;
		}
		return null;
	}

	public EmbedBuilder setEmbed(EmbedBuilder embedBuilder) {
		if (embedBuilder == null) {
			embedBuilder = new EmbedBuilder();
		}
		Question question = this.akiWrapper.getCurrentQuestion();
		embedBuilder.setDescription("**" + question.getQuestion() + "**");
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

		String message = event.getMessage().getContentRaw().trim();

		ThreadPool.run(() -> {
			Answer answer = this.parseAnswer(message);
			if (answer != null) {
				channel.sendTyping().complete();

				EmbedBuilder embedBuilder = new EmbedBuilder();

				List<Guess> guesses = this.akiWrapper.getGuessesAboveProbability(0.85);
				for (Guess guess : guesses) {
					if (!guess.getDescription().equals("---")) {
						embedBuilder.setAuthor("あなたが想像しているのは...");
						embedBuilder.setDescription(guess.getName() + " (" + guess.getDescription() + ")");
						embedBuilder.setImage(guess.getImage().toExternalForm());
						embedBuilder.addField("正解！", "1", true);
						embedBuilder.addField("違うので続ける...", "2", true);

						this.checkEnd = true;
						channel.sendMessageEmbeds(embedBuilder.build()).complete();
					}
				}

				for (Guess guess : this.akiWrapper.getGuesses()) {
					if (guess.getDescription() != null && !guess.getDescription().equals("---")) {
						System.out.println(guess.getDescription());
					}
				}

				if (!this.checkEnd) {
					this.akiWrapper.answerCurrentQuestion(answer);

					this.setEmbed(embedBuilder);
					channel.sendMessageEmbeds(embedBuilder.build()).complete();
				}
			}
		});
	}

}
