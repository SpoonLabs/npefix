package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import org.json.JSONObject;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;

public class  VariableInstance<T> extends AbstractInstance<T> {

	private String variableName;
	public VariableInstance(String variableName) {
		this.variableName = variableName;
	}
	@Override
	public T getValue() {
		Object o = CallChecker.getCurrentMethodContext().getVariables().get(variableName);
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

	@Override
	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("instanceType", getClass().getSimpleName().replace("Instance", ""));
		output.put("variableName", variableName);
		return output;
	}

	public CtVariableAccess<Object> toCtExpression(Factory factory) {
		CtVariableRead<Object> variableRead = factory.Core().createVariableRead();
		CtLocalVariableReference<Object> localVariableReference = factory.Core().createLocalVariableReference();
		localVariableReference.setSimpleName(variableName);
		variableRead.setVariable(localVariableReference);
		return variableRead;
	}
}
