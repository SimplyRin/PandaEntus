package net.simplyrin.pandaentus.commands;

import java.util.Random;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;

/**
 * Created by SimplyRin on 2020/07/09.
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
public class OutenCommand extends BaseCommand {

    @Override
    public String getCommand() {
        return "!outen";
    }
    
    @Override
    public String getDescription() {
        return "おうてん";
    }
    
    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl("outen", this.getDescription());
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
        int min = 1;
        int max = 3;

        int random = new Random().nextInt(min, max + 1);

        event.reply("https://api.rin.pink/pandaentus/" + random + ".jpg");
    }
}
