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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		VariableInstance<?> that = (VariableInstance<?>) o;

		if (variableName != null ?
				!variableName.equals(that.variableName) :
				that.variableName != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return variableName != null ? variableName.hashCode() : 0;
	}

	@Override
	public String toString() {
		return variableName;
	}
}
