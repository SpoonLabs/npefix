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
	private Location location;

	public MethodContext(Class c) {
		this(c, -1, -1, -1);
		Thread thread = Thread.currentThread();
		StackTraceElement[] stackTraces = thread.getStackTrace();
		StackTraceElement stackTrace = stackTraces[2];
		int line = stackTrace.getLineNumber();
		this.location = new Location(stackTrace.getClassName(), line, -1, -1);
	}

	public MethodContext(Class c, int line, int sourceStart, int sourceEnd) {
		CallChecker.methodStart(this);
		this.methodType = c;
		this.variables = new HashMap<String, Object>();

		int stackPosition = 2;
		if (this.getLocation() != null) {
			stackPosition --;
		}
		Thread thread = Thread.currentThread();
		StackTraceElement[] stackTraces = thread.getStackTrace();
		StackTraceElement stackTrace = stackTraces[stackPosition];
		methodName = stackTrace.getMethodName();
		className = stackTrace.getClassName();
		this.location = new Location(stackTrace.getClassName(), line, sourceStart, sourceEnd);
		this.id = idCount++;
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

	public Location getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "#" + id + " " + className + "#" + methodName + " " + variables.size() + " variables at " + this.getLocation();
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
