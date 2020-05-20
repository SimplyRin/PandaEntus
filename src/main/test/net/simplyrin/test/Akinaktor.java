package net.simplyrin.test;

import java.io.IOException;
import java.util.Scanner;

import com.markozajc.akiwrapper.Akiwrapper;
import com.markozajc.akiwrapper.Akiwrapper.Answer;
import com.markozajc.akiwrapper.AkiwrapperBuilder;
import com.markozajc.akiwrapper.core.entities.Question;
import com.markozajc.akiwrapper.core.entities.Server.Language;

/**
 * Created by SimplyRin on 2020/03/18.
 *
 * Copyright (c) 2020 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class Akinaktor {

	public static void main(String[] args) {
		Akiwrapper aw = new AkiwrapperBuilder().setLocalization(Language.JAPANESE).build();

		Scanner scanner = new Scanner(System.in);
		while (true) {
			Question q = aw.getCurrentQuestion();

			System.out.println(q.getQuestion());

			Answer answer = getAnswer();
			try {
				aw.answerCurrentQuestion(answer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static Answer getAnswer() {
		Scanner scanner = new Scanner(System.in);

		if (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			scanner.close();
			switch (line) {
			case "はい":
				return Answer.YES;
			case "いいえ":
				return Answer.NO;
			case "わからない":
			case "分からない":
				return Answer.DONT_KNOW;
			case "たぶんそう":
			case "部分的にそう":
				return Answer.PROBABLY;
			case "たぶん違う":
			case "そうでもない":
				return Answer.PROBABLY_NOT;
			}
		}

		return null;
	}

}
