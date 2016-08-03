package fr.inria.spirals.npefix.patch.sorter.algorithm;

import fr.inria.spirals.npefix.patch.sorter.Token;
import fr.inria.spirals.npefix.patch.sorter.Tokens;

public abstract class Algorithm {
	private final Token token;
	private final Tokens predicate;
	private final Tokens corpus;

	public Algorithm(Token token, Tokens predicate, Tokens corpus) {
		this.token = token;
		this.predicate = predicate;
		this.corpus = corpus;
	}

	public Token getToken() {
		return token;
	}

	public Tokens getPredicate() {
		return predicate;
	}

	public Tokens getCorpus() {
		return corpus;
	}

	public abstract double perform();
}
