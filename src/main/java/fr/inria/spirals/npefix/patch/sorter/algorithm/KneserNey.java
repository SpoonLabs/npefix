package fr.inria.spirals.npefix.patch.sorter.algorithm;

import fr.inria.spirals.npefix.patch.sorter.Token;
import fr.inria.spirals.npefix.patch.sorter.Tokens;

import java.util.Set;

/**
 * The Kneser-Ney smoothing algorithm has a notion of continuation probability
 * which helps with these sorts of cases.
 * It also saves you from having to recalculate all your counts using Good-Turing smoothing.
 */
public class KneserNey extends Algorithm {
	private final Set<Tokens> allBiGram;
	private double d = 0.1;

	public KneserNey(Token token, Tokens predicate, Tokens corpus) {
		super(token, predicate, corpus);
		this.allBiGram = getCorpus().getAllNGram(2);
	}

	public double perform() {
		return perform(getPredicate());
	}

	private double perform(Tokens predicate) {
		double countPrefix = getCorpus().count(predicate.get(predicate.fullSize() - 1));
		double count = getCorpus().count(getToken(), predicate);
		double countWordCanFollowPredicate = getCorpus().countWordCanFollow(predicate);

		// [ max( countkn( wi-n+1i ) - d, 0) ] / [ countkn( wi-n+1i-1 ) ]
		double probability = Math.max(count - d, 0) / countPrefix;
		// d * [ Num words that can follow wi-1 ] } / [ count( wi-1 )
		double normalisation = d * countWordCanFollowPredicate / countPrefix;
		double recursive;
		if (predicate.fullSize() == 1) {
			int countWordCanPrefixToken = getCorpus().countWordCanPrefix(getToken());
			int countBigram = allBiGram.size();
			// This is the number of bigrams where wi followed by wi-1,
			// divided by the total number of bigrams that appear with a frequency > 0.
			recursive = countWordCanPrefixToken / (double) countBigram;
		} else {
			//  Pkn( wi | wi-n+2i-1 )
			recursive = perform(predicate.subList(1, predicate.fullSize()));
		}
		return probability + normalisation * recursive;
	}
}
