package fr.inria.spirals.npefix.patch.sorter;

import fr.inria.spirals.npefix.patch.sorter.tokenizer.Tokenizer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * recursively iterates over a directory and enumerates all probabilityPatch of size n
 */
public class FileTokensCreator {
	Iterator<File> files;
	SingleFileTokenIterator it;
	int n;
	Tokens tokens;

	public FileTokensCreator(File f, int n, Tokenizer tokenizer) {
		files = FileUtils.listFiles(f, new String[] { "java" }, true).iterator();
		tokens = new Tokens(tokenizer);
		this.n = n;
		nextFile();
	}

	private void nextFile() {
		try {
			it = createTokenIterator(files.next(), n);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private SingleFileTokenIterator createTokenIterator(File f, int n) {
		try {
			return new SingleFileTokenIterator(f, n);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean hasNext() {
		while (!it.hasNext() && files.hasNext()) {
			nextFile();
		}
		return it.hasNext();
	}

	private Token next() {
		while (!it.hasNext() && files.hasNext()) {
			nextFile();
		}
		Token token = it.next();
		tokens.add(token);
		return token;
	}

	public Tokens getTokens() {
		while (hasNext()) {
			next();
		}
		return tokens;
	}
}
