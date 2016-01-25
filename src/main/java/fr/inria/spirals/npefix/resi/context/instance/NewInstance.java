package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.exception.ErrorInitClass;

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
				parameterTypes[i] = getClass().forName(s);
			}
			return (T) getClass().forName(clazz).getConstructor(parameterTypes).newInstance(objectParam);
		} catch (Exception e) {
			throw new ErrorInitClass("Unable to create the new instance of " + parameterType);
		} finally {
			if(wasEnable) {
				CallChecker.enable();
			}
		}
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
