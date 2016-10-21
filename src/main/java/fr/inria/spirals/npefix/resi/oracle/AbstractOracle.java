package fr.inria.spirals.npefix.resi.oracle;

import org.json.JSONObject;

public class AbstractOracle implements Oracle {
	private  boolean isValid;
	private String error;
	private String type = "test";

	public AbstractOracle(String type, boolean isValid)  {
		this.isValid = isValid;
		this.type = type;
	}

	@Override
	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean valid) {
		isValid = valid;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
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
	 * Print the stack trace of an exception
	 * @param exception
	 * @return
	 */
	protected String printException(Throwable exception) {
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

	@Override
	public String toString() {
		return error ;
	}
}
