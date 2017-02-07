package ru.innopolis.fdudinskiy.uniqcheck;

import ru.innopolis.fdudinskiy.uniqcheck.exceptions.IllegalSymbolsException;
import ru.innopolis.fdudinskiy.uniqcheck.exceptions.WordAlredyAddedException;
import ru.innopolis.fdudinskiy.uniqcheck.exceptions.WrongResourceException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fedinskiy on 06.02.17.
 */
public class ResourceChecker {
	private String resourceName;
	private List<String> wordArray;

	public ResourceChecker(String filePath) throws WrongResourceException, IOException, IllegalSymbolsException {
		final String DEFAULT_ENCODING = "UTF-8";
		File resource;

		if (null == filePath) {
			throw new WrongResourceException("Не передан путь!");
		}

		resource = new File(filePath);
		if (!resource.exists()) {
			throw new WrongResourceException("Файл " + filePath + " не существует");
		}
		if (!resource.isFile()) {
			throw new WrongResourceException("По пути " + filePath + " расположена папка!");
		}
		if (!resource.canRead()) {
			throw new WrongResourceException("Невозможно прочесть файл " + filePath + " !");
		}
		resourceName = resource.getAbsolutePath();

		try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(resource), DEFAULT_ENCODING))){
			read(in, Math.toIntExact(resource.length()));
			in.close();
		}catch (ArithmeticException ae){
			throw new WrongResourceException("Файл " + filePath + " слишком велик!");
		}

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	private void read( BufferedReader in, int size) throws IOException, IllegalSymbolsException {
		final String SPACES = "[\\s]";
		String line;
		String[] lineContent;
		String word;

		wordArray = new ArrayList<>(size);
		line = in.readLine();
		while (null != line) {
			if (line.isEmpty()) {
				line = in.readLine();
				continue;
			}
			if (!checkString(line)) {
				throw new IllegalSymbolsException(line, resourceName);
			}
			lineContent = line.split(SPACES);
			for (String stringPiece : lineContent) {
				word=cleanWord(stringPiece);
				if (!word.isEmpty()) {
					wordArray.add(word);
				}
			}
			line = in.readLine();
		}
	}

	private String cleanWord(String stringPiece) {
		final String NOT_WORD = "[^\\p{IsAlphabetic}-]+";

		final String PUNCTUATTION="[\\p{Punct}—]+";
		return stringPiece.replaceAll(NOT_WORD,"");
	}

	public void checkForRepeats(WordsStore store) throws IllegalSymbolsException, WordAlredyAddedException, IOException {
		for(String word:this.wordArray){
			store.addNewWord(word);
		}
		/*
		line = in.readLine();
		while (null != line) {
			if (line.isEmpty()) {
				line = in.readLine();
				continue;
			}
			if (!checkString(line)) {
				throw new IllegalSymbolsException(line, resourceName);
			}
			lineContent = line.split(SPACES);
			for (String word : lineContent) {
				if (!isNotWord(word)) {
					v
				}
			}
			line = in.readLine();
		}*/
	}


/*
	private boolean isNotWord(String word) {
		return word.matches(NOT_WORD);
	}

 */

	private boolean checkString(String wordForCheck) {
		final String ALLOWED_SYMBOLS = "[А-Яа-яЁё0-9\\s\\d,.\\-—?!№%\":*();]*";

		System.out.println("Обработка строки " + wordForCheck);
		return wordForCheck.matches(ALLOWED_SYMBOLS);
	}
}