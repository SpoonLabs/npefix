package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;

/**
 * the unmodified list of token
 */
public class FullTokenizer extends AbstractTokenizer {

	@Override
	public String computeRepresentation(Token token) {
		return token.getValue();
	}

}
