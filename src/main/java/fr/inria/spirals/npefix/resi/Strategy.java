package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.resi.exception.ErrorInitClass;
import fr.inria.spirals.npefix.resi.exception.VarNotFound;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

		/*Stack<Set<Object>> stack = CallChecker.getStack();
		for (Iterator<Set<Object>> iterator = stack.iterator(); iterator.hasNext(); ) {
			vars = iterator.next();
			object = obtainInstance(clazz, vars);
			if (object != null) return object;
		}*/
		throw new VarNotFound("cannot found var: " + clazz);
	}

	private <T> T obtainInstance(Class<?> clazz, Set<Object> vars) {
		for (Iterator<Object> iterator = vars.iterator(); iterator.hasNext(); ) {
			Object object = iterator.next();
			if(object!=null && object.getClass()!=null && clazz.isAssignableFrom(object.getClass())){
				return (T) object;
			}
		}
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(!Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			if(field.getType().isAssignableFrom(clazz)) {
				try {
					Object o = field.get(null);
					if(o != null) {
						return (T) o;
					}
				} catch (IllegalAccessException e) {
					continue;
				}
			}
		}
		return null;
	}

	public <T> T init(Class<?> clazz) {
		if(clazz==null)
			return null;
		if(clazz.isArray()) {
			T t = (T) Array.newInstance(clazz.getComponentType(), 100);
			for (int i = 0; i< 100; i++) {
				Array.set(t,i,init(clazz.getComponentType()));
			}
			return t;
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
			return (T) new Boolean(true);
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
		throw new fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError("missing primitive: "+clazz);
	}

	protected <T> T  initNotNull(Class<?> clazz){
		if(clazz==null)
			return null;
		/*if(clazz.equals(Class.class)) {
			throw new ErrorInitClass("cannot new instance " + clazz);
		}*/
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}
		List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
		if(clazz.isInterface() ||
				Modifier.isAbstract(clazz.getModifiers()) ||
				constructors.size() == 0) {
			clazz = getImplForInterface(clazz);
			if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
				throw new fr.inria.spirals.npefix.resi.exception.ErrorInitClass("missing interface " + clazz);
			}
		}
		constructors = Arrays.asList(clazz.getConstructors());
		Object res = null;
		try {
			res = (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			try{
				if(constructors.size() == 0) {
					constructors = Arrays.asList(clazz.getDeclaredConstructors());
				}
				if(constructors.size() == 0) {
					constructors.add(clazz.getEnclosingConstructor());
				}
				if(constructors.size() == 0) {
					throw new fr.inria.spirals.npefix.resi.exception.ErrorInitClass("missing constructor " + clazz);
				}
				Collections.sort(constructors,
						new Comparator<Constructor<?>>() {
							@Override
							public int compare(Constructor<?> c1,
									Constructor<?> c2) {
								int countPrimitifC1 = 0;
								int countPrimitifC2 = 0;
								for (int i = 0; i < c1.getParameterTypes().length; i++) {
									Class<?> aClass = c1.getParameterTypes()[i];
									if(aClass.isPrimitive()) {
										countPrimitifC1 ++;
									}
								}
								for (int i = 0; i < c2.getParameterTypes().length; i++) {
									Class<?> aClass = c2.getParameterTypes()[i];
									if(aClass.isPrimitive()) {
										countPrimitifC2 ++;
									}
								}
								if(countPrimitifC1 == countPrimitifC2) {
									return c1.getParameterTypes().length - c2.getParameterTypes().length;
								} else {
									return countPrimitifC2 - countPrimitifC1;
								}
							}
						});
				for (Constructor<?> constructor : constructors) {
					try{
						Class<?>[] types = constructor.getParameterTypes();
						// cannot use the Class constructor
						if(!constructor.isAccessible() &&
								constructor.getDeclaringClass() == Class.class) {
							continue;
						}
						if(!constructor.isAccessible() &&
								constructor.getDeclaringClass() != Class.class) {
							constructor.setAccessible(true);
						}
						List<Object> params = new ArrayList<>();
						try{
							for (int i = 0; i < types.length; i++) {
								if(!types[i].equals(clazz)) {
									params.add(init(types[i]));
								}
							}
						}catch (Throwable t){
							t.printStackTrace();
							continue;
						}
						if(params.size() != types.length) {
							continue;
						}
						res = (T) constructor.newInstance(params.toArray());
						return (T) res;
					}catch (Throwable t){
						System.err.println("Unable call constructor " + constructor);
					}
				}
			} catch (Throwable t){
				System.err.println("Unable new instance " + clazz);
			}
			throw new ErrorInitClass("cannot new instance "+clazz);
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
	public int hashCode() {
		return  this.getClass().getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.getClass().getSimpleName().equals(
				obj.getClass().getSimpleName());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
