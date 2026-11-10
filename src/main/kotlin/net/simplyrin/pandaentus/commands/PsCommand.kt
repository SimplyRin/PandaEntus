package net.simplyrin.pandaentus.commands

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.internal.interactions.CommandDataImpl
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent
import net.simplyrin.pandaentus.tts.TtsVoice

/**
 * このチャンネルの発言の読み上げ (Open JTalk) を開始/停止する。
 */
class PsCommand : BaseCommand() {

	override val command = "!ps"

	override val description = "このチャンネルの発言を読み上げます (もう一度実行すると停止)"

	override val commandData: CommandData
		get() = CommandDataImpl("ps", this.description)

	override val type = CommandType.EqualsIgnoreCase

	override val permission = CommandPermission.Everyone

	override fun execute(instance: PandaEntus, event: PandaMessageEvent, args: Array<String>) {
		val manager = instance.ttsManager
		val guild = event.guild
		val textChannelId = event.channel.idLong

		// このチャンネルを読み上げ中なら停止する
		val session = manager.getSession(guild.idLong)
		if (session != null && session.textChannelId == textChannelId) {
			manager.stop(guild)
			event.reply("🔇 読み上げを停止しました。")
			return
		}

		if (!manager.openJTalk.isReady()) {
			event.reply("Open JTalk を使用できません。サーバーに open_jtalk、辞書、音声モデル (.htsvoice) を導入して、config.yml の OpenJTalk を設定してください。")
			return
		}

		val voiceChannel = event.member.voiceState?.channel
		if (voiceChannel == null) {
			event.reply("ボイスチャンネルに接続してから実行してください。")
			return
		}

		val connectedChannel = guild.audioManager.connectedChannel
		if (connectedChannel != null && connectedChannel.idLong != voiceChannel.idLong) {
			event.reply("Bot は別のボイスチャンネル (" + connectedChannel.name + ") に参加しています。")
			return
		}

		manager.start(guild, textChannelId, voiceChannel)

		val message = StringBuilder()
		if (session != null) {
			message.append("🔊 読み上げるチャンネルを " + event.channel.asMention + " に変更しました。")
		} else {
			message.append("🔊 " + event.channel.asMention + " の発言を読み上げます。(" + this.command + " でもう一度実行すると停止)")
		}
		message.append("\n先頭に `;` を付けた発言は読み上げません。")

		val voice = TtsVoice.forUser(event.user.idLong, manager.openJTalk.voices)
		if (voice != null) {
			message.append("\nあなたの声: " + voice.describe())
		}

		event.reply(message.toString())
	}

}
