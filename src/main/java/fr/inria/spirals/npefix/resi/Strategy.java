package fr.inria.spirals.npefix.resi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public abstract class Strategy {

	public abstract <T> T isCalled(T o, Class<?> clazz);

	public abstract <T> T returned(Class<?> clazz);
	
	public abstract boolean beforeDeref(Object called);

	protected <T> T obtain(Class<?> clazz) {
		if(clazz==null)
			return null;
		Map<String, Object> vars = CallChecker.getCurrentVars();
		for (Object object : vars.values()) {
			if(object!=null && object.getClass()!=null && clazz.isAssignableFrom(object.getClass())){
				return (T) object;
			}
		}
		throw new AbnormalExecutionError("cannot found var: " + clazz);
	}
	
	public <T> T init(Class<?> clazz) {
		if(clazz==null)
			return null;
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}
		return null;
	}
	
	protected <T> T  initPrimitive(Class<?> clazz){
		if(clazz == int.class){
			return (T) new Integer(0);
		}
		if(clazz == char.class){
			return (T) new Character(' ');
		}
		if(clazz == boolean.class){
			return (T) new Boolean(false);
		}
		if(clazz == float.class){
			return (T) new Float(0);
		}
		if(clazz == double.class){
			return (T) new Double(0);
		}
		if(clazz == long.class){
			return (T) new Long(0);
		}
		throw new AbnormalExecutionError("missing primitive:"+clazz);
	}

	protected <T> T  initNotNull(Class<?> clazz){
		if(clazz==null)
			return null;
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}
		if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			clazz = getImplForInterface(clazz);
			if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
				throw new AbnormalExecutionError("missing interface"+clazz);
			}
		}
		Object res = null;
		try {
			res = (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			System.err.println("cannot new instance "+clazz);
			try{
				for (Constructor<?> constructor : clazz.getConstructors()) {
					try{
						Class<?>[] types = constructor.getParameterTypes();
						Object[] params = new Object[types.length];
						for (int i = 0; i < types.length; i++) {
							try{
								if(types[i]==clazz)
									params[i]=null;
								else
									params[i] = init(types[i]);
							}catch (Throwable t){
								t.printStackTrace();
							}
						}
						res = (T) constructor.newInstance(params);
						return (T) res;
					}catch (Throwable t){
						t.printStackTrace();
					}
				}
			}catch (Throwable t){
				t.printStackTrace();
			}
		}
		return (T) res;
	}
	
	
	protected Class<?> getImplForInterface(Class<?> clazz) {
		if(!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
			return clazz;
		}
		if(clazz.isAssignableFrom(Set.class)){
			clazz =  HashSet.class;
		} else if(clazz.isAssignableFrom(Comparator.class)){
			clazz = new Comparator() {
				public int compare(Object o1, Object o2) {
					return 0;
				}
			}.getClass();
		} else if(clazz.isAssignableFrom(List.class)){
			clazz = ArrayList.class;
		} else if(clazz.isAssignableFrom(Map.class)){
			clazz = HashMap.class;
		} else {
			try {
				clazz = Class.forName(clazz.getCanonicalName() + "Impl");
			} catch (ClassNotFoundException e) {
				ClassLoader classLoader = clazz.getClassLoader();
				Class<?> CL_class = classLoader.getClass();
				while (CL_class != java.lang.ClassLoader.class) {
					CL_class = CL_class.getSuperclass();
				}
				java.lang.reflect.Field ClassLoader_classes_field = null;
				try {
					ClassLoader_classes_field = CL_class.getDeclaredField("classes");
				} catch (NoSuchFieldException e1) {
					return clazz;
				}
				ClassLoader_classes_field.setAccessible(true);
				Vector<Class<?>> classes = null;
				try {
					classes = (Vector<Class<?>>) ClassLoader_classes_field.get(classLoader);
				} catch (IllegalAccessException e1) {
					return clazz;
				}
				for (int i = 0; i < classes.size(); i++) {
					Class<?> cl = classes.elementAt(i);
					if(cl.isInterface()) {
						continue;
					}
					if( Modifier.isAbstract(cl.getModifiers())) {
						continue;
					}
					if(clazz.isAssignableFrom(cl)){
						return cl;
					}
				}
				
			}

		}
		return clazz;
	}
	
}
