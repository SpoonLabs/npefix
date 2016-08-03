package fr.inria.spirals.npefix.patch.sorter;

import fr.inria.spirals.npefix.patch.sorter.algorithm.Laplace;
import fr.inria.spirals.npefix.patch.sorter.tokenizer.Tokenizer;

import java.io.File;

public class Experiment {

	/**
	 * collects all n-grams and returns them
	 */
	public double probabilityPatch(
			int n,
			Tokens corpusTokens,
			String patch) {
		Tokenizer tokenizer = corpusTokens.getTokenizer();
		StringTokensCreator stringTokensCreator = new StringTokensCreator(patch, n, tokenizer);
		Tokens patchTokens = stringTokensCreator.getTokens();
		double[] probabilities = probability (patchTokens, corpusTokens, n);
		double probability = 1;
		for (int i = 0; i < probabilities.length; i++) {
			double p = probabilities[i];
			//System.out.println(String.format("%15s: %f", patchTokens.get(i), p));
			probability *= p;
		}
		if (probability == 0) {
			return Double.MAX_VALUE;
		}
		// perplexity.
		probability = Math.pow(probability, - 1 / (double) probabilities.length);
		return probability;
	}

	/**
	 * collects all n-grams and returns them
	 */
	public double probabilityPatch(File source,
			int n,
			Tokenizer tokenizer,
			String patch) {
		FileTokensCreator creator = new FileTokensCreator(source, n, tokenizer);
		Tokens corpusTokens = creator.getTokens();
		return probabilityPatch(n, corpusTokens, patch);
	}

	private double[] probability(Tokens patchTokens, final Tokens corpusTokens, int n) {
		double[]  result = new double[patchTokens.fullSize()];
		for (int i = 0; i < patchTokens.fullSize(); i++) {
			Token token = patchTokens.get(i);
			Tokens predicate = patchTokens.subList(Math.max(i - n + 1, 0), Math.max(i, 0));
			if (predicate.isEmpty()) {
				result[i] = 1;
			} else {
				//result[i] = new NGram(token, predicate, corpusTokens).perform();
				//result[i] = new KneserNey(token, predicate, corpusTokens).perform();
				result[i] = new Laplace(token, predicate, corpusTokens).perform();
			}
		}
		return result;
	}
}
