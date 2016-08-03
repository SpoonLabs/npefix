package fr.inria.spirals.npefix.patch.sorter.tokenizer;

import fr.inria.spirals.npefix.patch.sorter.Token;

public interface Tokenizer {

	String computeRepresentation(Token token);
}