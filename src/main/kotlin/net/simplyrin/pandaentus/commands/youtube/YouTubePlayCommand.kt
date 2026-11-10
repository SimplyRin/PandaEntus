package net.simplyrin.pandaentus.commands.youtube

import java.awt.Color

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.utils.ThreadPool

/**
 * Created by SimplyRin on 2020/07/09.
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
class YouTubePlayCommand : BaseCommand() {

	override val command = "!play"

	override val description = "曲を追加/再生"

	override val commandData: CommandData
		get() = CommandDataImpl("play", this.description)
			.addOption(OptionType.STRING, "url", "YouTube, Twitch, Bandcamp の URL を入力", true)

	override val aliases = listOf("!p")

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val embedBuilder = EmbedBuilder()

		val arguments = if (event.isSlashCommand()) {
			arrayOf(this.command, event.slashCommandEvent?.getOption("url")?.asString.orEmpty())
		} else {
			args
		}

		val guild = event.guild

		if (arguments.size > 1) {
			val url = arguments[1]

			if (!url.startsWith("http")) {
				event.reply("検索機能を使用することはできません。URL を入力してください。")
				return
			}

			val voiceChannel = event.member.voiceState?.channel
			if (voiceChannel == null) {
				event.reply("ボイスチャンネルに接続してください。")
				return
			}

			// プレイリストの URL は対象外
			val urls = if (url.contains("list")) emptyList() else listOf(url)

			embedBuilder.setColor(Color.RED)
			embedBuilder.setAuthor("準備しています...")
			val message = event.reply(embedBuilder.build())

			ThreadPool.run {
				val footer = "追加: " + this.command + ", スキップ: " + instance.commandNameOf(YouTubeSkipCommand::class.java) +
					", シャッフル: " + instance.commandNameOf(YouTubeShuffleCommand::class.java)

				val audioManager = guild.audioManager
				audioManager.openAudioConnection(voiceChannel)
				audioManager.setAutoReconnect(false)
				audioManager.setSelfDeafened(true)

				val musicManager = instance.getGuildAudioPlayer(guild)

				val messages = ArrayList<MessageEmbed>()

				for (urlItem in urls) {
					instance.playerManager.loadItemOrdered(musicManager, urlItem, object : AudioLoadResultHandler {

						override fun trackLoaded(track: AudioTrack) {
							embedBuilder.setAuthor(null)
							embedBuilder.clearFields()
							embedBuilder.addField("🎵 タイトル", track.info.title, true)
							embedBuilder.addField("💿 アーティスト", track.info.author, true)
							embedBuilder.setFooter(footer)

							messages.add(embedBuilder.build())

							embedBuilder.setFooter("詳細: " + instance.commandNameOf(YouTubeNowPlayingCommand::class.java) +
								", コマンド一覧: " + instance.commandNameOf(YouTubeHelpCommand::class.java))

							instance.previousTrack[guild.idLong] = track
							instance.play(guild, musicManager, track)

							if (messages.size == 1) {
								message.editMessageEmbeds(messages[0]).complete()
							}
						}

						override fun playlistLoaded(playlist: AudioPlaylist) {
							instance.audioPlaylist = playlist

							val firstTrack = playlist.selectedTrack ?: playlist.tracks.firstOrNull() ?: return

							if (instance.loopMap[guild.idLong] == null) {
								instance.play(guild, musicManager, firstTrack)
							}
						}

						override fun noMatches() {
							message.editMessage("曲が見つかりませんでした。").complete()
						}

						override fun loadFailed(exception: FriendlyException) {
							message.editMessage("曲を読み込めませんでした。\n" + exception.message).complete()
						}

					})
				}
			}
			return
		}

		val help = instance.commandRegister.getRegisteredCommand(YouTubeHelpCommand::class.java) as? YouTubeHelpCommand ?: return
		val helpEmbed = help.getHelpEmbed(instance) ?: return
		helpEmbed.setColor(Color.RED)
		helpEmbed.setAuthor("使用方法: " + arguments.firstOrNull().orEmpty() + " <YouTube, Twitch, Bandcamp>")
		helpEmbed.setDescription(help.getHelpMessage())

		event.reply(helpEmbed.build())
	}

}
