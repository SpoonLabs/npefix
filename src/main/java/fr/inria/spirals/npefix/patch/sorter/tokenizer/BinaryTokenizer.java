package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;

/**
 * a suite of IDs or Java keywords
 */
public class BinaryTokenizer extends AbstractTokenizer {

	@Override
	public String computeRepresentation(Token token) {
		if (token.isOperator() || token.isKeyword()) {
			return "JAVA$";
		}
		return "ID$";
	}

}
