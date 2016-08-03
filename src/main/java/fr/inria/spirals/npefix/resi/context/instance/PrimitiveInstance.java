package fr.inria.spirals.npefix.resi.context.instance;

import org.json.JSONObject;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;

public class  PrimitiveInstance<T> extends AbstractInstance<T> {

	public T value;

	public PrimitiveInstance(T value) {
		this.value = value;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "" + value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		PrimitiveInstance<?> that = (PrimitiveInstance<?>) o;

		if (value != null ? !value.equals(that.value) : that.value != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("instanceType", getClass().getSimpleName().replace("Instance", ""));
		if (value == null) {
			output.put("class", "null");
			output.put("value", "null");
		} else {
			output.put("class", value.getClass());
			output.put("value", value);
		}
		return output;
	}

	public CtLiteral toCtExpression(Factory factory) {
		return factory.Code().createLiteral(value);
	}
}
