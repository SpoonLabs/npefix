package fr.inria.spirals.npefix.patch.sorter.algorithm;

import fr.inria.spirals.npefix.patch.sorter.Token;
import fr.inria.spirals.npefix.patch.sorter.Tokens;

public class Laplace extends Algorithm {
	public Laplace(Token token, Tokens predicate, Tokens corpus) {
		super(token, predicate, corpus);
	}

	public double perform() {
		int countWithPredicate = getCorpus().count(getToken(), getPredicate());
		int countTotal = countWithPredicate;
		if (!getPredicate().isEmpty()) {
			countTotal = getCorpus().nbWordCanFollow(getPredicate()) + getCorpus().size();
		}
		return (countWithPredicate  + 1) / (double) countTotal;
	}
}
