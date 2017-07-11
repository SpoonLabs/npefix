package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import org.json.JSONObject;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtLocalVariableReference;

public class ArrayReadInstance<T> extends AbstractInstance<T> {

	private String variableName;
	private int index;

	public ArrayReadInstance(String variableName, int index) {
		this.variableName = variableName;
		this.index = index;
	}

	@Override
	public T getValue() {
		Object o = CallChecker.getCurrentMethodContext().getVariables().get(variableName);
		return (T) o;
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
		output.put("index", index);
		return output;
	}

	@Override
	public CtArrayRead toCtExpression(Factory factory) {
		CtArrayRead<?> variableRead = factory.Core().createArrayRead();
		CtLocalVariableReference<?> localVariableReference = factory.Core().createLocalVariableReference();
		localVariableReference.setSimpleName(variableName);
		variableRead.setTarget(factory.createVariableRead(localVariableReference, false));
		variableRead.setIndexExpression(factory.createLiteral(index));
		return variableRead;
	}
}
