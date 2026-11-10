package net.simplyrin.pandaentus.commands.youtube

import java.awt.Color
import java.net.URI
import java.net.URISyntaxException

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.EmbedBuilder
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

			// ローカルファイルの再生を有効にしているため、http(s) の URL 以外 (ファイルのパスなど) は受け付けない
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				event.reply("検索機能を使用することはできません。URL を入力してください。")
				return
			}

			val voiceChannel = event.member.voiceState?.channel
			if (voiceChannel == null) {
				event.reply("ボイスチャンネルに接続してください。")
				return
			}

			// プレイリストには対応していない (YouTube へのリクエストが増えてレートリミットに掛かりやすくなるため)。
			// 動画の URL についているプレイリストの指定は無視して、その動画だけを再生する
			val videoUrl = this.stripPlaylist(url)
			if (videoUrl == null) {
				event.reply(PLAYLIST_NOT_SUPPORTED)
				return
			}

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

				instance.playerManager.loadItemOrdered(musicManager, videoUrl, object : AudioLoadResultHandler {

					override fun trackLoaded(track: AudioTrack) {
						embedBuilder.setAuthor(null)
						embedBuilder.clearFields()
						embedBuilder.addField("🎵 タイトル", track.info.title, true)
						embedBuilder.addField("💿 アーティスト", track.info.author, true)
						embedBuilder.setFooter(footer)

						instance.previousTrack[guild.idLong] = track
						instance.play(guild, musicManager, track)

						message.editMessageEmbeds(embedBuilder.build()).complete()
					}

					// SoundCloud のセットや Bandcamp のアルバムなど、プレイリストとして読み込まれたものは再生しない
					override fun playlistLoaded(playlist: AudioPlaylist) {
						message.editMessage(PLAYLIST_NOT_SUPPORTED).setEmbeds(emptyList()).complete()
					}

					override fun noMatches() {
						message.editMessage("曲が見つかりませんでした。").complete()
					}

					override fun loadFailed(exception: FriendlyException) {
						message.editMessage("曲を読み込めませんでした。\n" + exception.message).complete()
					}

				})
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

	/**
	 * YouTube の URL からプレイリスト関連のパラメータ (list, index など) を取り除く。
	 * プレイリスト自体を指している URL (動画を含まないもの) の場合は null を返す。
	 * YouTube 以外の URL はそのまま返す。
	 */
	private fun stripPlaylist(url: String): String? {
		val uri = try {
			URI(url)
		} catch (e: URISyntaxException) {
			return url
		}

		val host = uri.host?.lowercase() ?: return url
		if (host != "youtu.be" && host != "youtube.com" && !host.endsWith(".youtube.com")) {
			return url
		}

		val params = uri.rawQuery.orEmpty().split("&").filter { it.isNotEmpty() }
		val kept = params.filter { it.substringBefore("=") !in PLAYLIST_PARAMS }

		val path = uri.path.orEmpty()
		val hasList = params.any { it.substringBefore("=") == "list" } || path == "/playlist"
		val hasVideo = (host == "youtu.be" && path.length > 1) ||
			kept.any { it.substringBefore("=") == "v" } ||
			VIDEO_PATHS.any { path.startsWith(it) }

		if (!hasVideo) {
			// 動画を含まない: プレイリストならば対象外、それ以外 (チャンネルなど) は従来通り読み込みを試みる
			return if (hasList) null else url
		}

		if (kept.size == params.size) {
			return url
		}

		val base = url.substringBefore("#").substringBefore("?")
		return if (kept.isEmpty()) base else base + "?" + kept.joinToString("&")
	}

	private companion object {

		const val PLAYLIST_NOT_SUPPORTED = "プレイリストには対応していません。動画の URL を入力してください。"

		// プレイリストに関するクエリパラメータ
		val PLAYLIST_PARAMS = setOf("list", "index", "start_radio", "pp")

		// パスに動画 ID を含む URL の形式
		val VIDEO_PATHS = listOf("/shorts/", "/live/", "/embed/")

	}

}
