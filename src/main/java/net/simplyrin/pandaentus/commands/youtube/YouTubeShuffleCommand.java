package net.simplyrin.pandaentus.commands.youtube;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

import java.util.List;

public class YouTubeShuffleCommand extends BaseCommand {

    @Override
    public String getCommand() {
        return "!shuffle";
    }

    @Override
    public String getDescription() {
        return "プレイリストをシャッフル";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("shuffle", this.getDescription());
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
        return CommandPermission.Everyone;
    }

    @Override
    public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);

        AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();
        if (voiceChannel == null) {
            event.reply("ボイスチャンネルに接続してください。");
            return;
        }

        AudioTrack audioTrack = musicManager.getPlayer().getPlayingTrack();
        if (audioTrack == null) {
            BaseCommand playCommand = instance.getCommandRegister().getRegisteredCommand(YouTubePlayCommand.class);
            event.reply("現在何も再生していません。\n" + playCommand.getCommand() + " コマンドを利用して音楽を再生することができます。");
            return;
        }

        musicManager.getScheduler().shuffle();
        event.reply("プレイリストをシャッフルしました。");
    }

}
