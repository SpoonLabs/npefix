package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;

public class  VariableInstance<T> implements Instance<T> {

	private String variableName;
	public VariableInstance(String variableName) {
		this.variableName = variableName;
	}
	@Override
	public T getValue() {
		Object o = CallChecker.getCurrentMethodContext().getVariables()
				.get(variableName);
		return (T) o;
	}

	@Override
	public String toString() {
		return variableName;
	}
}
