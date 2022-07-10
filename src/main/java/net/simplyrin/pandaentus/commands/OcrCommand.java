package net.simplyrin.pandaentus.commands;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.io.FileUtils;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.requests.Requester;
import net.simplyrin.pandaentus.PandaEntus;
import net.simplyrin.pandaentus.classes.BaseCommand;
import net.simplyrin.pandaentus.classes.CommandPermission;
import net.simplyrin.pandaentus.classes.CommandType;
import net.simplyrin.pandaentus.classes.PandaMessageEvent;
import net.sourceforge.tess4j.Tesseract;

/**
 * Created by SimplyRin on 2021/11/30.
 *
 * Copyright (C) 2021 SimplyRin
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
public class OcrCommand extends BaseCommand {

	@Override
	public String getCommand() {
		return "!ocr";
	}

	@Override
	public String getDescription() {
		return "画像から文字を解析";
	}

	@Override
	public CommandData getCommandData() {
		return new CommandDataImpl("ocr", this.getDescription())
				.addOption(OptionType.STRING, "言語", "jpn, eng");
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
		if (event.isSlashCommand()) {
			var s = event.getSlashCommandEvent();
			
			args = new String[2];
			args[0] = this.getCommand();
			args[1] = s.getOption("言語") != null ? s.getOption("言語").getAsString() : "jpn";
		}
		
		var message = event.getMessage();
		var attachments = message.getAttachments();
		
		if (attachments.isEmpty()) {
			event.reply("画像ファイルを付けてもう一度実行してください。");
			return;
		}
		
		for (var attachment : attachments) {
			var extension = attachment.getFileExtension();
			if (!(extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg"))) {
				event.reply(".JPG, .PNG のみ対応しています。");
				continue;
			}
			
			File ocr = new File("ocr");
			ocr.mkdirs();
			
			File file = new File(ocr, UUID.randomUUID().toString().split("-")[0] + "." + extension);
			
			try {
				HttpsURLConnection connection = (HttpsURLConnection) new URL(attachment.getUrl()).openConnection();
				connection.addRequestProperty("user-agent", Requester.USER_AGENT);
				
				InputStream is = connection.getInputStream();
				
				FileUtils.copyInputStreamToFile(is, file);
				
				var tess = new Tesseract();
				tess.setDatapath(new File("tessdata").getAbsolutePath());
				tess.setLanguage("jpn");
				
				String value = tess.doOCR(file);
				
				event.reply("```" + value + "```");
			} catch (Exception e) {
				event.reply("解析に失敗しました。");
			}
		}
	}

}
