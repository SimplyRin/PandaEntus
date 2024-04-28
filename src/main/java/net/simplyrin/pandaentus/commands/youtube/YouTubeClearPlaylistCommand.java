package net.simplyrin.pandaentus.commands.youtube;

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

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class YouTubeClearPlaylistCommand extends BaseCommand {

    @Override
    public String getCommand() {
        return "!clearplaylist";
    }

    @Override
    public String getDescription() {
        return "プレイリストを削除";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("clearplaylist", this.getDescription());
    }

    @Override
    public List<String> getAlias() {
        return Arrays.asList("!cp");
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
        musicManager.getScheduler().clearPlaylist();

        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setDescription("プレイリストを削除しました。");

        event.reply(embedBuilder.build());

    }
}
