package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;

/**
 * literals  are abstracted as LIT$1,LIT$2, etc
 */
public class RenameSyntaxTokenizer extends AbstractTokenizer {

	@Override
	public String computeRepresentation(Token token) {
		if (token.isSyntax()) {
			return ("SYN$");
		}
		return token.getValue();
	}

}
