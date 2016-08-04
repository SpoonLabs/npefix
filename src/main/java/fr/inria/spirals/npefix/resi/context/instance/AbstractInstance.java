package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.CallChecker;
import org.json.JSONObject;

public abstract class AbstractInstance<T> implements Instance<T> {

	public Class getClassFromString (String className) {
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
			try {
				return CallChecker.currentClassLoader.loadClass(className);
			} catch (Exception e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	public abstract JSONObject toJSON();

	@Override
	public int compareTo(Object o) {
		if (this instanceof PrimitiveInstance) {
			return -5;
		}
		if (this instanceof VariableInstance) {
			return -4;
		}
		if (this instanceof StaticVariableInstance) {
			return -3;
		}
		if (this instanceof NewInstance) {
			return -2;
		}
		if (this instanceof NewArrayInstance) {
			return -1;
		}
		return 0;
	}
}
