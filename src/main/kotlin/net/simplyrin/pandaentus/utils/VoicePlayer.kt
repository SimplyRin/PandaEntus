package net.simplyrin.pandaentus.utils

import java.io.File

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.simplyrin.pandaentus.PandaEntus

/**
 * 生成した音声ファイルを、メンバーが参加しているボイスチャンネルで再生する
 */
object VoicePlayer {

	@JvmStatic
	fun play(instance: PandaEntus, guild: Guild, member: Member, channel: MessageChannel, file: File) {
		val voiceChannel = member.voiceState?.channel ?: return

		val audioManager = guild.audioManager
		audioManager.openAudioConnection(voiceChannel)
		audioManager.setAutoReconnect(false)

		val musicManager = instance.getGuildAudioPlayer(guild)

		file.deleteOnExit()

		instance.playerManager.loadItemOrdered(musicManager, file.absolutePath, object : AudioLoadResultHandler {

			override fun trackLoaded(track: AudioTrack) {
				instance.play(guild, musicManager, track)
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				val firstTrack = playlist.selectedTrack ?: playlist.tracks.firstOrNull() ?: return

				instance.play(guild, musicManager, firstTrack)
			}

			override fun noMatches() {
			}

			override fun loadFailed(exception: FriendlyException) {
				channel.sendMessage("Could not play: " + exception.message).queue()
			}

		})
	}

}
