package net.simplyrin.pandaentus.listeners

import java.util.concurrent.ConcurrentHashMap

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import net.simplyrin.pandaentus.PandaEntus
import net.simplyrin.pandaentus.classes.BaseCommand
import net.simplyrin.pandaentus.classes.CommandPermission
import net.simplyrin.pandaentus.classes.CommandType
import net.simplyrin.pandaentus.classes.PandaMessageEvent

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
class CommandExecutor(private val instance: PandaEntus) : ListenerAdapter() {

	private val map = ConcurrentHashMap<String, BaseCommand>()

	fun registerCommand(commands: CommandListUpdateAction, command: String?, baseCommand: BaseCommand) {
		requireNotNull(command) { baseCommand.javaClass.name + "#getCommand() is null!" }

		this.map[command] = baseCommand
		println("[Command:Register] $command")

		val commandData = baseCommand.commandData
		if (commandData != null) {
			println("[Command:Register|Slash] $command")
			commands.addCommands(commandData)
		}

		for (alias in baseCommand.aliases.orEmpty()) {
			println("[Command:Register-Alias] - $alias")
		}
	}

	fun unregisterCommand(command: String) {
		this.map.remove(command)
		println("[Command:Un-Register] $command")
	}

	fun getCommand(command: String): BaseCommand? {
		return this.map[command]
	}

	fun getRegisteredCommand(clazz: Class<*>): BaseCommand? {
		return this.map.values.lastOrNull { it.javaClass == clazz }
	}

	override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		val guild = event.guild ?: return

		val user = event.user
		if (user.isBot) {
			return
		}

		val raw = this.getCommandPath(event)
		val args = raw.split("/").toMutableList()

		args[0] = "!" + args[0]

		println(guild.name + "@" + guild.idLong + ", " + user.name + ": " + raw + " (" + args[0] + ")")

		val pandaMessageEvent = PandaMessageEvent.get(event) ?: return

		this.check(args.toTypedArray(), user, pandaMessageEvent, raw)
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.isFromType(ChannelType.PRIVATE)) {
			return
		}

		val user = event.author
		if (user.isBot) {
			return
		}

		val raw = event.message.contentRaw

		// Java の String#split と同様に、末尾の空要素は取り除く
		val args = raw.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
		if (args.isEmpty()) {
			return
		}

		val pandaMessageEvent = PandaMessageEvent.get(event) ?: return

		this.check(args, user, pandaMessageEvent, raw)
	}

	fun check(args: Array<String>, user: User, event: PandaMessageEvent, raw: String) {
		for (baseCommand in this.map.values) {
			val commands = listOfNotNull(baseCommand.command) + baseCommand.aliases.orEmpty()

			// 名前やエイリアスが複数一致しても、1 つのコマンドは 1 回だけ実行する
			val matched = commands.any { command ->
				when (baseCommand.type) {
					CommandType.EqualsIgnoreCase -> args[0].equals(command, ignoreCase = true)
					CommandType.StartsWith -> args[0].lowercase().startsWith(command.lowercase())
					CommandType.Contains -> args[0].lowercase().contains(command.lowercase())
					null -> false
				}
			}

			if (matched) {
				this.execute(baseCommand, user, event, raw, args)
			}
		}
	}

	fun execute(baseCommand: BaseCommand, user: User, event: PandaMessageEvent, raw: String, args: Array<String>) {
		if (baseCommand.permission == CommandPermission.BotOwner && !this.instance.isBotOwner(user)) {
			return
		}

		if (baseCommand.permission == CommandPermission.ServerAdministrator
			&& !event.member.hasPermission(Permission.MANAGE_SERVER)) {
			return
		}

		println(user.name + " (" + user.id + "), Executed: " + raw)

		if (baseCommand.sendTyping()) {
			event.channel.sendTyping().complete()
		}

		baseCommand.execute(this.instance, event, args)
	}

	fun getCommandPath(event: SlashCommandInteractionEvent): String {
		val builder = StringBuilder(event.name)

		val subcommandGroup = event.subcommandGroup
		if (subcommandGroup != null) {
			builder.append('/').append(subcommandGroup)
		}

		val subcommandName = event.subcommandName
		if (subcommandName != null) {
			builder.append('/').append(subcommandName)
		}

		return builder.toString()
	}

}
