package net.simplyrin.pandaentus.commands.botowner;

import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2025/08/16.
 *
 * Copyright (C) 2025 SimplyRin
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
public class OutenMasterCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!outenmaster";
	}

	@Override
	public String getDescription() {
		return "これであなたも横転マスター！";
	}

	@Override
	public CommandType getType() {
		return CommandType.EqualsIgnoreCase;
	}

	@Override
	public CommandPermission getPermission() {
		return CommandPermission.BotOwner;
	}

	@Override
	public void execute(PandaEntus instance, PandaMessageEvent event, String[] args) {
		if (args.length > 1) {
            try {
                int i = Integer.parseInt(args[1]);
                instance.getDataConfig().set("OutenMaster", i);

                event.reply("おうてんマスターの数を " + i + " に設定しました。");
                return;
            } catch (Exception e) {
            }
            
        }

        event.reply("おうてんマスターの数を設定するには、`!outenmaster <数>` の形式でコマンドを実行してください。");
	}

}
