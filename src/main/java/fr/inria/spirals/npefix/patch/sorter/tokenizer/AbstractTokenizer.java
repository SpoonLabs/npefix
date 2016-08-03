package fr.inria.spirals.npefix.patch.sorter.tokenizer;

public abstract class AbstractTokenizer implements Tokenizer {


	public AbstractTokenizer() {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().replace("Tokenizer", "");
	}
}
