package fr.inria.spirals.npefix.resi;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class Strategy {
	public abstract <T> T isCalled(T o, Class<?> clazz);

	public abstract <T> T returned(Class<?> clazz);
	
	public abstract boolean beforeDeref(Object called);

	public boolean collectData() {
		return false;
	}

	public <T> T beforeCalled(T o, Class<?> clazz) {
		return o;
	}

	protected <T> T obtain(Class<?> clazz) {
		if(clazz == null) {
			return null;
		}

		Set<Object> vars = CallChecker.getCurrentVars();
		T object = obtainInstance(clazz, vars);

		if (object != null) return object;

		Stack<Set<Object>> stack = CallChecker.getStack();
		for (Iterator<Set<Object>> iterator = stack.iterator(); iterator.hasNext(); ) {
			vars = iterator.next();
			object = obtainInstance(clazz, vars);
			if (object != null) return object;
		}
		throw new AbnormalExecutionError("cannot found var: " + clazz);
	}

	private <T> T obtainInstance(Class<?> clazz, Set<Object> vars) {
		for (Iterator<Object> iterator = vars.iterator(); iterator.hasNext(); ) {
			Object object = iterator.next();
			if(object!=null && object.getClass()!=null && clazz.isAssignableFrom(object.getClass())){
				return (T) object;
			}
		}
		return null;
	}

	public <T> T init(Class<?> clazz) {
		if(clazz==null)
			return null;
		if(clazz.isArray()) {
			return (T) Array.newInstance(clazz.getComponentType(), 0);
		}
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
		if(clazz == byte.class){
			return (T) new Byte((byte)0);
		}
		if(clazz == short.class){
			return (T) new Short((short) 0);
		}
		throw new AbnormalExecutionError("missing primitive:"+clazz);
	}

	protected <T> T  initNotNull(Class<?> clazz){
		if(clazz==null)
			return null;
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}
		Constructor<?>[] constructors = clazz.getConstructors();
		if(clazz.isInterface() ||
				Modifier.isAbstract(clazz.getModifiers()) ||
				constructors.length == 0) {
			clazz = getImplForInterface(clazz);
			if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
				throw new AbnormalExecutionError("missing interface " + clazz);
			}
		}
		constructors = clazz.getConstructors();
		Object res = null;
		try {
			res = (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			try{
				if(constructors.length == 0) {
					constructors = clazz.getDeclaredConstructors();
				}
				if(constructors.length == 0) {
					constructors = new Constructor[]{clazz.getEnclosingConstructor()};
				}
				if(constructors.length == 0) {
					throw new AbnormalExecutionError("missing constructor " + clazz);
				}
				for (Constructor<?> constructor : constructors) {
					try{
						Class<?>[] types = constructor.getParameterTypes();
						// cannot use the Class constructor
						if(Modifier.isPrivate(constructor.getModifiers()) &&
								constructor.getDeclaringClass() == Class.class) {
							continue;
						}
						if(Modifier.isPrivate(constructor.getModifiers()) &&
								constructor.getDeclaringClass() != Class.class) {
							constructor.setAccessible(true);
						}
						List<Object> params = new ArrayList<>();
						for (int i = 0; i < types.length && !types[i].equals(clazz); i++) {
							try{
								params.add(init(types[i]));
							}catch (Throwable t){
								t.printStackTrace();
							}
						}
						if(params.size() != types.length) {
							continue;
						}
						res = (T) constructor.newInstance(params.toArray());
						return (T) res;
					}catch (Throwable t){
						t.printStackTrace();
					}
				}
			} catch (Throwable t){
				t.printStackTrace();
			}
			//throw new AbnormalExecutionError("cannot new instance "+clazz);
		}
		return (T) res;
	}
	
	protected Class<?> getImplForInterface(Class<?> clazz) {
		if(!(clazz.isInterface() ||
				Modifier.isAbstract(clazz.getModifiers()) ||
				clazz.getConstructors().length == 0)) {
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
				if(classLoader == null) {
					return clazz;
				}
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
					if (cl.getConstructors().length == 0) {
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

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
