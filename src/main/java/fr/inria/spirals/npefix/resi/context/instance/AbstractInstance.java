package fr.inria.spirals.npefix.resi.context.instance;

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
			throw new RuntimeException(e);
		}
	}

	public abstract JSONObject toJSON();
}
