package fr.inria.spirals.npefix.resi.context.instance;

import org.json.JSONObject;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.factory.Factory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class NewArrayInstance<T> extends AbstractInstance<T> {

	private final String clazz;
	private final int level;
	private List<Instance<?>> values;

	public NewArrayInstance(String clazz, List<Instance<?>> values) {
		this.clazz = clazz;
		this.values = values;
		this.level = 1;
	}

	public NewArrayInstance(Class<T> clazz, List<Instance<?>> values) {
		if(!clazz.isArray()) {
			throw new IllegalArgumentException(clazz + " is not an array");
		}
		Class<?> componentType = clazz.getComponentType();
		int level = 1;
		while(componentType.isArray()) {
			level ++;
			componentType = componentType.getComponentType();
		}
		this.clazz = componentType.getCanonicalName();
		this.level = level;
		this.values = values;
	}

	@Override
	public T getValue() {
		Class<?> aClass = getClassFromString(clazz);
		T t = (T) Array.newInstance(aClass, values.size());
		for (int i = 1; i< level; i++) {
			t = (T) Array.newInstance(t.getClass(), values.size());
		}
		for (int i = 0; i < values.size(); i++) {
			Instance<?> value = values.get(i);
			Array.set(t, i, value.getValue());
		}
		return t;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		NewArrayInstance<?> that = (NewArrayInstance<?>) o;

		if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null)
			return false;
		if (this.values.size() != that.values.size()) {
			return false;
		}
		for (int i = 0; i < values.size(); i++) {
			Instance<?> instance = values.get(i);
			Instance<?> instanceThat = that.values.get(i);
			if (!instance.equals(instanceThat)) {
				return false;
			}
		}
		if (level != that.level) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = clazz != null ? clazz.hashCode() : 0;
		result = 31 * result + level;
		return result;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(clazz);
		for (int j = 0; j < level; j++) {
			stringBuilder.append("[]");
			stringBuilder.append("{");
			for (int i = 0; i < values.size(); i++) {
				Instance<?> instance = values.get(i);
				stringBuilder.append(instance.toString());
				if (i + 1 < values.size()) {
					stringBuilder.append(", ");
				}
			}
			stringBuilder.append("}");
		}
		return stringBuilder.toString();
	}

	@Override
	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("instanceType", getClass().getSimpleName().replace("Instance", ""));
		output.put("class", clazz);
		output.put("level", level);
		for (int i = 0; i < values.size(); i++) {
			Instance<?> instance = values.get(i);
			output.append("values", instance.toJSON());
		}
		return output;
	}

	public CtExpression toCtExpression(Factory factory) {
		CtNewArray<Object> newArray = factory.Core().createNewArray();
		List<CtExpression<Integer>> dimensions = new ArrayList<>(level);
		for (int i = 0; i < level; i++) {
			dimensions.add(factory.Code().createLiteral(values.size()));
		}
		newArray.setDimensionExpressions(dimensions);
		List<CtExpression<?>> elements = new ArrayList<>();
		for (int i = 0; i < values.size(); i++) {
			Instance<?> instance = values.get(i);
			elements.add(instance.toCtExpression(factory));
		}
		newArray.setType(factory.Class().createArrayReference(clazz));
		newArray.setElements(elements);
		return newArray;
	}
}
