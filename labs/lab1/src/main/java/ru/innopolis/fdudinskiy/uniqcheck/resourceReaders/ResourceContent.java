package ru.innopolis.fdudinskiy.uniqcheck.resourceReaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.innopolis.fdudinskiy.uniqcheck.exceptions.IllegalSymbolsException;
import ru.innopolis.fdudinskiy.uniqcheck.exceptions.ResourceTooLargeException;
import ru.innopolis.fdudinskiy.uniqcheck.exceptions.WordAlreаdyAddedException;
import ru.innopolis.fdudinskiy.uniqcheck.store.WordsStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by fedinskiy on 06.02.17.
 */

public abstract class ResourceContent {
	protected static Logger logger = LogManager.getLogger(ResourceContent.class);
	private String resourceName;
	private ArrayList<String> wordArray;
	
	protected ResourceContent(String resourceName) {
		this.resourceName = resourceName;
		this.wordArray = new ArrayList<>();
	}
	
	protected void prepareStore(long dataSize) throws ResourceTooLargeException {
		final int MAX_WORD_TO_SIZE_RATIO = 2;
			if ((dataSize / MAX_WORD_TO_SIZE_RATIO)+1 > Integer.MAX_VALUE) {
			throw new ResourceTooLargeException("Ресурс слишком велик!");
		}
		this.wordArray = new ArrayList<String>(
				(int) (dataSize / MAX_WORD_TO_SIZE_RATIO));
	}
	
	public synchronized void addLine(String line) throws IllegalSymbolsException {
		addLineToArray(line);
	}
	
	private void addLineToArray(String line) throws IllegalSymbolsException {
		final String SPACES = "[\\s]";
		String word;
		
		String[] lineContent;
		if (line.isEmpty()) {
			return;
		}
		Integer i = new Integer("1");
		if (!isStringContainsAcceptableSymbolsOnly(line)) {
			throw new IllegalSymbolsException(line, resourceName);
		}
		lineContent = line.split(SPACES);
		for (String stringPiece : lineContent) {
			word = cleanWord(stringPiece);
			if (!word.isEmpty()) {
				wordArray.add(word);
			}
		}
	}
	
	protected void read(BufferedReader in)
			throws IOException, IllegalSymbolsException {
		String line;
		if (null == wordArray) {
			wordArray = new ArrayList<>();
		}
		
		line = in.readLine();
		while (null != line) {
			addLineToArray(line);
			line = in.readLine();
		}
		wordArray.trimToSize();
	}
	
	private String cleanWord(String stringPiece) {
		final String NOT_WORD = "[^\\p{IsAlphabetic}-]+";
		
		return stringPiece.replaceAll(NOT_WORD, "");
	}
	
	/**
	 * @param store
	 * @throws WordAlreаdyAddedException
	 * @implSpec Проверяет все слова, прочитанные из ресурса,
	 * на наличие в данном хранилище.
	 */
	public boolean addNewWordsToStore(WordsStore store) throws WordAlreаdyAddedException {
		for (String word : this.wordArray) {
			logger.trace("пишем слово " + word + " из источника " + this
					.resourceName);
			if (!store.addNewWord(word))
				break;
		}
		return !store.isHasDoubles();
	}
	
	private boolean isStringContainsAcceptableSymbolsOnly(String wordForCheck) {
		final String ALLOWED_SYMBOLS = "[А-Яа-яЁё0-9«»\\s\\d\\,\\.\\-\\-\\?\\—\\–!№%\":*();" +
				"\\[\\]]*";
		logger.trace("Обработка строки " + wordForCheck);
		return wordForCheck.matches(ALLOWED_SYMBOLS);
	}
	
	public int getSize() {
		return wordArray.size();
	}
}
