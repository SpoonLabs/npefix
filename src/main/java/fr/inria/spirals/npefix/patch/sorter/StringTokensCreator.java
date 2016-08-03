package fr.inria.spirals.npefix.patch.sorter;

import fr.inria.spirals.npefix.patch.sorter.tokenizer.Tokenizer;

/**
 * recursively iterates over a directory and enumerates all probabilityPatch of size n
 */
public class StringTokensCreator {
	StringTokenIterator it;
	int n;
	Tokens tokens;

	public StringTokensCreator(String f, int n, Tokenizer tokenizer) {
		this.n = n;
		it = new StringTokenIterator(f, n);
		tokens = new Tokens(tokenizer);
	}

	public Tokens getTokens() {
		while (it.hasNext()) {
			tokens.add(it.next());
		}
		return tokens;
	}
}
