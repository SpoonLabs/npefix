package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;

/**
 * identifiers and literals are abstracted as ID$1,ID$2, etc
 */
public class RenameIdentifierLiteralTokenizer extends AbstractTokenizer {


	@Override
	public String computeRepresentation(Token token) {
		if (token.isText()) {
			return ("ID$");
		}
		return token.getValue();
	}

}
