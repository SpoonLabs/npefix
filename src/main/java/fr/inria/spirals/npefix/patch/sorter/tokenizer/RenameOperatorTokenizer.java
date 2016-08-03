package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;
/**
 * literals  are abstracted as LIT$1,LIT$2, etc
 */
public class RenameOperatorTokenizer extends AbstractTokenizer {

	@Override
	public String computeRepresentation(Token token) {
		if (token.isOperator()) {
			return ("OP");
		}
		return token.getValue();
	}

}
