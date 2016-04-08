package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.CallChecker;

import java.util.HashMap;

public class MethodContext {

	public static int idCount = 1;
	private final HashMap<String, Object> variables;
	private final Class  methodType;
	private final String methodName;
	private final String className;
	private final int id;

	public MethodContext(Class c) {
		CallChecker.methodStart(this);
		this.methodType = c;
		this.variables = new HashMap<String, Object>();
		Thread thread = Thread.currentThread();
		StackTraceElement[] stackTraces = thread.getStackTrace();
		StackTraceElement stackTrace = stackTraces[2];
		methodName = stackTrace.getMethodName();
		className = stackTrace.getClassName();
		this.id = idCount++;
	}

	public <T> T methodSkip() {
		// TODO Auto-generated method stub
		return null;
	}

	public void methodEnd() {
		CallChecker.methodEnd(this);
	}

	public HashMap<String, Object> getVariables() {
		return variables;
	}

	public void addVariable(String name, Object value) {
		variables.put(name, value);
	}

	public Class getMethodType() {
		return methodType;
	}

	@Override
	public String toString() {
		return "#" + id + " " + className + "#" + methodName + " (" + variables.size() + " variables)";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MethodContext that = (MethodContext) o;

		return this.id == that.id;
	}

	@Override
	public int hashCode() {
		int result = methodName != null ? methodName.hashCode() : 0;
		result = 31 * result + (className != null ? className.hashCode() : 0);
		return result;
	}
}
