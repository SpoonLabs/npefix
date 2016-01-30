package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.exception.ErrorInitClass;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

public class NewInstance<T> implements Instance<T> {

	private final String clazz;
	private String[] parameterType;
	private List<Instance<?>> parameters;

	public NewInstance(String clazz, String[] parameterType,
			List<Instance<?>> parameters) {
		this.clazz = clazz;
		this.parameterType = parameterType;
		this.parameters = parameters;
	}

	private Class getClassFromString (String className) {
		if(className.equals("int")) {
			return int.class;
		}
		if(className.equals("int[]")) {
			return int[].class;
		}
		if(className.equals("long")) {
			return long.class;
		}
		if(className.equals("long[]")) {
			return long[].class;
		}
		if(className.equals("float")) {
			return float.class;
		}
		if(className.equals("float[]")) {
			return float[].class;
		}
		if(className.equals("double")) {
			return double.class;
		}
		if(className.equals("double[]")) {
			return double[].class;
		}
		if(className.equals("byte")) {
			return byte.class;
		}
		if(className.equals("byte[]")) {
			return byte[].class;
		}
		if(className.equals("char")) {
			return char.class;
		}
		if(className.equals("char[]")) {
			return char[].class;
		}
		if(className.equals("boolean")) {
			return boolean.class;
		}
		if(className.equals("boolean[]")) {
			return boolean[].class;
		}
		try {
			return getClass().forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	@Override
	public T getValue() {
		boolean wasEnable = CallChecker.isEnable();
		CallChecker.disable();
		Object[] objectParam = new Object[parameters.size()];
		for (int i = 0; i < parameters.size(); i++) {
			Instance<?> instance = parameters.get(i);
			objectParam[i] = instance.getValue();
		}
		try {
			Class[] parameterTypes = new Class[parameterType.length];
			for (int i = 0; i < parameterType.length; i++) {
				String s = parameterType[i];
				parameterTypes[i] = getClassFromString(s);
			}
			Constructor<?> constructor = getClass().forName(clazz)
					.getConstructor(parameterTypes);
			constructor.setAccessible(true);
			return (T) constructor.newInstance(objectParam);
		} catch (Exception e) {
			throw new ErrorInitClass("Unable to create the new instance of " + parameterType, e);
		} finally {
			if(wasEnable) {
				CallChecker.enable();
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		NewInstance<?> that = (NewInstance<?>) o;

		if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null)
			return false;
		// Probably incorrect - comparing Object[] arrays with Arrays.equals
		if (!Arrays.equals(parameterType, that.parameterType))
			return false;
		if (parameters != null ?
				!parameters.equals(that.parameters) :
				that.parameters != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = clazz != null ? clazz.hashCode() : 0;
		result = 31 * result + Arrays.hashCode(parameterType);
		result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(clazz);
		stringBuilder.append("(");
		for (int i = 0; i < parameters.size(); i++) {
			Instance<?> instance = parameters.get(i);
			stringBuilder.append(instance.toString());
			if(i + 1 < parameters.size()) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
}
