package fr.inria.spirals.npefix.resi.oracle;

import org.json.JSONObject;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.List;

public class TestOracle implements Oracle {
	private  boolean isValid;
	private String error;
	private String type = "test";

	public TestOracle(Result r)  {
		isValid = r.wasSuccessful();
		if(!isValid) {
			error = toStringFailures(r);
		}
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject resultJSON = new JSONObject();
		resultJSON.put("success", isValid);
		resultJSON.put("error", error);
		resultJSON.put("type", type);
		return resultJSON;
	}

	/**
	 * Get the string representation of the failure of a junit result
	 * @param r the junit result
	 * @return the string representation of the failures
	 */
	private String toStringFailures(Result r) {
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

	/**
	 * Print the stack trace of an exception
	 * @param exception
	 * @return
	 */
	private String printException(Throwable exception) {
		StringBuilder output = new StringBuilder();
		output.append(exception.getClass() + ": " + exception.getMessage() + "\n");
		for (int i = 0; i < exception.getStackTrace().length && i < 25; i++) {
			StackTraceElement trace = exception.getStackTrace()[i];
			output.append("    at " + trace.getClassName() + '.' + trace.getMethodName());
			output.append('(' + trace.getFileName() + ':' + trace.getLineNumber() + ")\n");
		}
		if(exception.getCause() != null) {
			output.append("Caused By:\n");
			output.append(printException(exception.getCause()));
		}
		return output.toString();
	}
}
