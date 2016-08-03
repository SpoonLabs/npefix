package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;

/**
 * identifiers are abstracted as ID$1,ID$2, etc
 */
public class RenameIdentifierTokenizer extends AbstractTokenizer {

	@Override
	public String computeRepresentation(Token token) {
		if (token.getType() == ITerminalSymbols.TokenNameIdentifier) {
			return ("ID$");
		}
		return token.getValue();
	}

}
