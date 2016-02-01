package fr.inria.spirals.npefix.resi.oracle;

import org.junit.runner.Result;

public class TestOracle extends AbstractOracle {

	public TestOracle(Result r)  {
		super("test", r.wasSuccessful());
		if(!isValid()) {
			setError(toStringFailures(r));
		}
	}
}
