package fr.inria.spirals.npefix.resi.context.instance;

import java.lang.reflect.Array;
import java.util.List;

public class ArrayInstance<T> implements Instance<T> {

	private final String clazz;
	private List<Instance<?>> values;

	public ArrayInstance(String clazz, List<Instance<?>> values) {
		this.clazz = clazz;
		this.values = values;
	}

	@Override
	public T getValue() {
		try {
			Class<?> aClass = getClass().forName(clazz);
			T t = (T) Array.newInstance(aClass, values.size());
			for (int i = 0; i < values.size(); i++) {
				Instance<?> value = values.get(i);
				Array.set(t, i, value.getValue());
			}
			return t;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ArrayInstance<?> that = (ArrayInstance<?>) o;

		if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null)
			return false;
		if (values != null ? !values.equals(that.values) : that.values != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = clazz != null ? clazz.hashCode() : 0;
		result = 31 * result + (values != null ? values.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(clazz);
		stringBuilder.append("[");
		for (int i = 0; i < values.size(); i++) {
			Instance<?> instance = values.get(i);
			stringBuilder.append(instance.toString());
			if(i + 1 < values.size()) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
}
