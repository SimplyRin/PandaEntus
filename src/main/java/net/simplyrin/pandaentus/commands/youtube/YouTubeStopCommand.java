package net.simplyrin.pandaentus.commands.youtube;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.audio.GuildMusicManager;
import net.simplyrin.pandaentus.audio.TrackScheduler;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

import java.util.Arrays;
import java.util.List;

public class YouTubeStopCommand extends BaseCommand {

    @Override
    public String getCommand() {
        return "!stop";
    }

    @Override
    public String getDescription() {
        return "再生中の曲を停止";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("stop", this.getDescription());
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
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = instance.getGuildAudioPlayer(guild);

        AudioChannel voiceChannel = event.getMember().getVoiceState().getChannel();
        if (voiceChannel == null) {
            channel.sendMessage("ボイスチャンネルに接続してください。").complete();
            return;
        }

        AudioTrack audioTrack = musicManager.getPlayer().getPlayingTrack();
        if (audioTrack == null) {
            BaseCommand playCommand = instance.getCommandRegister().getRegisteredCommand(YouTubePlayCommand.class);
            channel.sendMessage("現在何も再生していません。\n" + playCommand.getCommand() + " コマンドを利用して音楽を再生することができます。").complete();
            return;
        }

        musicManager.getPlayer().stopTrack();
        musicManager.getScheduler().clearPlaylist();

        instance.getGuildAudioPlayer(guild).getPlayer().destroy();
        guild.getAudioManager().closeAudioConnection();

        channel.sendMessage("停止しました。").complete();
        musicManager.getScheduler().updateVoiceStatus(null, TrackScheduler.TrackStatus.END);
    }

}
