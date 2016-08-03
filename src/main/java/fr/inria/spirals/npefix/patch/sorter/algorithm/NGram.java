package fr.inria.spirals.npefix.patch.sorter.algorithm;

import fr.inria.spirals.npefix.patch.sorter.Token;
import fr.inria.spirals.npefix.patch.sorter.Tokens;

public class NGram extends Algorithm {
	public NGram(Token token, Tokens predicate, Tokens corpus) {
		super(token, predicate, corpus);
	}

	public double perform() {
		int countWithPredicate = getCorpus().count(getToken(), getPredicate());
		int countTotal = countWithPredicate;
		if (!getPredicate().isEmpty()) {
			countTotal = getCorpus().count(getPredicate().get(getPredicate().fullSize() - 1), getPredicate().subList(0, getPredicate().fullSize() - 1));
		}
		if (countTotal == 0) {
			return 0.0;
		}
		return countWithPredicate / (double) countTotal;
	}
}
