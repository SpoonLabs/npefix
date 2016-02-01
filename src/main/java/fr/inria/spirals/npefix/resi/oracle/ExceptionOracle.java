package fr.inria.spirals.npefix.resi.oracle;

public class ExceptionOracle extends AbstractOracle {

	public ExceptionOracle(Exception e) {
		super("exception", false);
		setError(printException(e));
	}
}
