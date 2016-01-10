package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.exception.ErrorInitClass;

import java.lang.reflect.Constructor;
import java.util.List;

public class NewInstance<T> implements Instance<T> {

	private Constructor<?> constructor;
	private List<Instance<?>> parameters;

	public NewInstance(Constructor<?> constructor,
			List<Instance<?>> parameters) {
		this.constructor = constructor;
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
			return (T) constructor.newInstance(objectParam);
		} catch (Exception e) {
			throw new ErrorInitClass("Unable to create the new instance of " + constructor);
		} finally {
			if(wasEnable) {
				CallChecker.enable();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(constructor.getDeclaringClass().getCanonicalName());
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
