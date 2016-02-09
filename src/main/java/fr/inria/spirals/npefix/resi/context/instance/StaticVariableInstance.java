package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.exception.VarNotFound;

import java.lang.reflect.Field;

public class StaticVariableInstance<T> extends AbstractInstance<T> {

	private final String clazz;
	private String fieldName;

	public StaticVariableInstance(String clazz, String fieldName) {
		this.clazz = clazz;
		this.fieldName = fieldName;
	}
	@Override
	public T getValue() {
		try {
			Field field = getClass().forName(clazz).getField(fieldName);
			field.setAccessible(true);
			Object o = field.get(null);
			return (T) o;
		} catch (Exception e) {
			throw new VarNotFound("Unable to get the fied of " + clazz);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		StaticVariableInstance<?> that = (StaticVariableInstance<?>) o;

		if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null)
			return false;
		if (fieldName != null ?
				!fieldName.equals(that.fieldName) :
				that.fieldName != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = clazz != null ? clazz.hashCode() : 0;
		result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return clazz + "." + fieldName;
	}
}
