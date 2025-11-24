package net.simplyrin.pandaentus.commands.youtube;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2020/07/11.
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
public class YouTubeQueueCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!queue";
	}

	@Override
	public String getDescription() {
		return "次に再生される曲を確認";
	}

	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("queue", this.getDescription());
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList("!playlist");
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.Everyone;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		EmbedBuilder embedBuilder = new EmbedBuilder();

		Guild guild = event.getGuild();

		GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);
		BlockingQueue<AudioTrack> queue = musicManager.getScheduler().getQueue();

		BaseCommand playCommand = instance.getCommandRegister().getRegisteredCommand(YouTubePlayCommand.class);
		BaseCommand skipCommand = instance.getCommandRegister().getRegisteredCommand(YouTubeSkipCommand.class);
		BaseCommand loopCommand = instance.getCommandRegister().getRegisteredCommand(YouTubeLoopCommand.class);
		BaseCommand shuffleCommand = instance.getCommandRegister().getRegisteredCommand(YouTubeShuffleCommand.class);

		AudioTrack at = instance.getLoopMap().get(guild.getIdLong());
		if (at != null) {
			embedBuilder.setColor(Color.CYAN);
			embedBuilder.setAuthor("🔁 ループ再生が有効になっています。");
			embedBuilder.setDescription("🎵 再生中の音楽: " + at.getInfo().title);
			embedBuilder.setFooter("詳細: !nowplaying, ループ無効: " + loopCommand.getCommand());

			event.reply(embedBuilder.build());
			return;
		}

		if (queue == null || queue.isEmpty()) {
			event.reply("次に再生が予定されている曲はありません。\n"
					+ playCommand.getCommand() + " を使用することで、プレイリストに追加することができます。");
			return;
		}

		String message = "";

		AudioTrack playingTrack = musicManager.getPlayer().getPlayingTrack();
		if (playingTrack != null) {
			embedBuilder.setAuthor("🎵 再生中の音楽: " + playingTrack.getInfo().title);
		}

		int i = 1;
		for (AudioTrack audioTrack : queue) {
			message += "**" + i + ":** __" + audioTrack.getInfo().title + "__\n";
			i++;
		}

		if (message.length() == 0) {
			event.reply("再生待ちの曲はありません。");
			return;
		}
		if (message.length() >= 1800) {
			message = message.substring(0, 1800);
		}
		embedBuilder.setDescription(message);
		embedBuilder.setColor(Color.CYAN);
		embedBuilder.setFooter("追加: " + playCommand.getCommand() + ", スキップ: " + skipCommand.getCommand() + ", シャッフル: "
				+ shuffleCommand.getCommand());

		event.reply(embedBuilder.build());
	}

}
