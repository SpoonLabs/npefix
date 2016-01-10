package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.CallChecker;

import java.util.HashMap;

public class MethodContext {

	private final HashMap<String, Object> variables;

	public MethodContext(Class c) {
		CallChecker.methodStart(this);
		this.variables = new HashMap<String, Object>();
	}

	public <T> T methodSkip() {
		// TODO Auto-generated method stub
		return null;
	}

	public void methodEnd() {
		CallChecker.methodEnd();
	}

	public HashMap<String, Object> getVariables() {
		return variables;
	}

	public void addVariable(String name, Object value) {
		variables.put(name, value);
	}
}
