package fr.inria.spirals.npefix.resi.oracle;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

public class TestOracle extends AbstractOracle {

	public TestOracle(Result r)  {
		super("test", r.wasSuccessful());
		if(!isValid()) {
			setError(toStringFailures(r));
		}
	}

	/**
	 * Get the string representation of the failure of a junit result
	 * @param r the junit result
	 * @return the string representation of the failures
	 */
	protected String toStringFailures(Result r) {
		StringBuilder output = new StringBuilder();
		List<Failure> failures = r.getFailures();
		for (int j = 0; j < failures.size(); j++) {
			Failure failure = failures.get(j);
			Throwable exception = failure.getException();
			output.append(failure.toString() + "\n");
			if(exception != null) {
				output.append(printException(exception));
				output.append("\n\n");
			}
		}

		return output.toString();
	}
}
