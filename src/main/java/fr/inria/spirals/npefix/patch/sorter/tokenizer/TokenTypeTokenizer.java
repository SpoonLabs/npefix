package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;

/**
 * a list of token types, ect PAREN, ID, OPERATOR
 */
public class TokenTypeTokenizer extends AbstractTokenizer {

	@Override
	public String computeRepresentation(Token token) {
		return token.getType() + "";
	}

}
