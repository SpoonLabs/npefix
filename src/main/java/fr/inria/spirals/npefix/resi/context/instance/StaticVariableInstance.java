package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.exception.VarNotFound;
import org.json.JSONObject;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

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
			Field field = CallChecker.currentClassLoader.loadClass(clazz).getField(fieldName);
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

	@Override
	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("instanceType", getClass().getSimpleName().replace("Instance", ""));
		output.put("class", clazz);
		output.put("fieldName", fieldName);
		return output;
	}

	public CtFieldRead toCtExpression(Factory factory) {
		CtFieldRead<Object> fieldRead = factory.Core().createFieldRead();
		CtFieldReference<Object> fieldReference = factory.Core().createFieldReference();
		CtTypeReference reference = factory.Class().createReference(clazz);
		fieldReference.setStatic(true);
		fieldReference.setSimpleName(fieldName);
		fieldReference.setDeclaringType(reference);
		fieldRead.setVariable(fieldReference);
		return fieldRead;
	}
}
