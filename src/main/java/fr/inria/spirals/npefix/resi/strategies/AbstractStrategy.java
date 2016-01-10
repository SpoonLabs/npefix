package fr.inria.spirals.npefix.resi.strategies;

import com.sun.org.apache.bcel.internal.generic.ExceptionThrower;
import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.context.instance.NewInstance;
import fr.inria.spirals.npefix.resi.context.instance.PrimitiveInstance;
import fr.inria.spirals.npefix.resi.context.instance.VariableInstance;
import fr.inria.spirals.npefix.resi.exception.ErrorInitClass;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

public abstract class AbstractStrategy implements Strategy {

	@Override
	public boolean collectData() {
		return false;
	}


	public static <T> Instance<T> initClass(Class<T> clazz) {
		if(clazz == null)
			return new PrimitiveInstance<T>(null);
		if(clazz.isArray()) {
			T t = (T) Array.newInstance(clazz.getComponentType(), 100);
			for (int i = 0; i < 100; i++) {
				Array.set(t,i, initClass(clazz.getComponentType()).getValue());
			}
			return new PrimitiveInstance<T>(t);
		}
		if(clazz.isPrimitive()){
			List<Instance<T>> instances = initPrimitive(clazz);
			return instances.get(0);
		}
		List<Instance<T>> instances = AbstractStrategy.<T>initNotNull(clazz);
		if(instances.size() > 0) {
			return instances.get(0);
		}
		return new PrimitiveInstance<>(null);
	}

	protected static <T> Map<String, Instance<T>> obtain(Class<?> clazz) {
		if(clazz == null || clazz == void.class) {
			return Collections.EMPTY_MAP;
		}

		MethodContext vars = CallChecker.getCurrentMethodContext();
		Map<String, Instance<T>> instances = obtainInstance(clazz, vars.getVariables());

		 return instances;

		/*Stack<Set<Object>> stack = CallChecker.getStack();
		for (Iterator<Set<Object>> iterator = stack.iterator(); iterator.hasNext(); ) {
			vars = iterator.next();
			object = obtainInstance(clazz, vars);
			if (object != null) return object;
		}*/
		//throw new VarNotFound("cannot found var: " + clazz);
	}

	private static Map<Class,Class> primitiveToClass = new HashMap<Class,Class>();{
		primitiveToClass.put(int.class, Integer.class);
		primitiveToClass.put(long.class, Long.class);
		primitiveToClass.put(double.class, Double.class);
		primitiveToClass.put(float.class, Float.class);
		primitiveToClass.put(boolean.class, Boolean.class);
		primitiveToClass.put(char.class, Character.class);
		primitiveToClass.put(byte.class, Byte.class);
		primitiveToClass.put(short.class, Short.class);
	}

	private static <T> Map<String, Instance<T>> obtainInstance(Class<?> clazz, Map<String, Object> vars) {
		if(clazz.isPrimitive()) {
			clazz = primitiveToClass.get(clazz);
		}
		Map<String, Instance<T>> instances = new HashMap<>();
		Collection<String> keys = vars.keySet();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
			String variable = iterator.next();
			Object object = vars.get(variable);
			if(object!=null && object.getClass()!=null && clazz.isAssignableFrom(object.getClass())){
				instances.put(variable, new VariableInstance<T>(variable));
			}
		}
		/*
		// search static  instance in the field
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(!Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			field.setAccessible(true);
			if(clazz.isAssignableFrom(field.getType())) {
				try {
					Object o = field.get(null);
					if(o != null) {
						instances.put(clazz.getCanonicalName() + "." + field.getName(), (T) o);
					}
				} catch (IllegalAccessException e) {
					continue;
				}
			}
		}*/
		return instances;
	}

	public static  <T> List<Instance<T>>  initPrimitive(Class<?> clazz){
		List<Instance<T>> instances = new ArrayList<>();
		if(clazz == int.class){
			instances.add(new PrimitiveInstance<T>((T) new Integer(0)));
			instances.add(new PrimitiveInstance<T>((T) new Integer(-1)));
			instances.add(new PrimitiveInstance<T>((T) new Integer(1)));
		}
		if(clazz == char.class){
			instances.add(new PrimitiveInstance<T>((T) new Character(' ')));
		}
		if(clazz == boolean.class){
			instances.add(new PrimitiveInstance<T>((T) new Boolean(true)));
			instances.add(new PrimitiveInstance<T>((T) new Boolean(false)));
		}
		if(clazz == float.class){
			instances.add(new PrimitiveInstance<T>((T) new Float(0)));
			instances.add(new PrimitiveInstance<T>((T) new Float(-1)));
			instances.add(new PrimitiveInstance<T>((T) new Float(1)));
		}
		if(clazz == double.class){
			instances.add(new PrimitiveInstance<T>((T) new Double(0)));
			instances.add(new PrimitiveInstance<T>((T) new Double(-1)));
			instances.add(new PrimitiveInstance<T>((T) new Double(1)));
		}
		if(clazz == long.class){
			instances.add(new PrimitiveInstance<T>((T) new Long(0)));
			instances.add(new PrimitiveInstance<T>((T) new Long(-1)));
			instances.add(new PrimitiveInstance<T>((T) new Long(1)));
		}
		if(clazz == byte.class){
			instances.add(new PrimitiveInstance<T>((T) new Byte((byte)0)));
			instances.add(new PrimitiveInstance<T>((T) new Byte((byte)1)));
		}
		if(clazz == short.class){
			instances.add(new PrimitiveInstance<T>((T) new Short((short) 0)));
			instances.add(new PrimitiveInstance<T>((T) new Short((short) -1)));
			instances.add(new PrimitiveInstance<T>((T) new Short((short) 1)));
		}
		return instances;
	}

	protected static <T> List<Instance<T>>  initNotNull(Class<?> clazz){
		if(clazz == null) {
			return Collections.EMPTY_LIST;
		}
		List<Class> classes = new ArrayList<>();
		classes.add(clazz);
		List<Instance<T>> instances = new ArrayList<>();
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}

		if(clazz.isInterface() ||Modifier.isAbstract(clazz.getModifiers()) || clazz.getConstructors().length == 0) {
			classes.addAll(getImplForInterface(clazz));
		}
		for (int i = 0; i < classes.size(); i++) {
			Class aClass = classes.get(i);
			instances.addAll(getInstancesClass(aClass));
		}
		return instances;
	}

	private static Stack<Class> stackInstance = new Stack<>();

	private static <T> List<Instance<T>> getInstancesClass(Class<T> clazz) {
		stackInstance.add(clazz);
		List<Instance<T>> instances = new ArrayList<>();
		if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			return instances;
		}
		List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
		if(constructors.isEmpty()) {
			constructors = Arrays.asList(clazz.getDeclaredConstructors());
		}
		if(constructors.isEmpty()) {
			constructors.add(clazz.getEnclosingConstructor());
		}
		if(constructors.isEmpty()) {
			//throw new ErrorInitClass("missing constructor " + clazz);
			return instances;
		}
		try{

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
			for (int j = 0; j < constructors.size(); j++) {
				Constructor<?> constructor = constructors.get(j);
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
					List<Instance<?>> params = new ArrayList<>();
					try{
						for (int i = 0; i < types.length; i++) {
							if(!stackInstance.contains(types[i])) {
								Instance<?> value = initClass(types[i]);
								value.getValue();
								params.add(value);
							} else {
								continue;
							}
						}
					}catch (Throwable t){
						t.printStackTrace();
						continue;
					}
					if(params.size() != types.length) {
						continue;
					}
					Instance<T> instance = new NewInstance<T>(constructor, params);
					try {
						instance.getValue();
						instances.add(instance);
					} catch (ErrorInitClass e) {
						System.err.println("Unable call constructor " + constructor);
						continue;
					}
				}catch (Throwable t){
					System.err.println("Unable call constructor " + constructor);
				}
			}
		} catch (Throwable t){
			System.err.println("Unable new instance " + clazz);
		} finally {
			stackInstance.remove(clazz);
		}
		return instances;
	}

	protected static List<Class> getImplForInterface(Class<?> clazz) {
		List<Class> classes = new ArrayList<>();
		if(!(clazz.isInterface() ||
				Modifier.isAbstract(clazz.getModifiers()) ||
				clazz.getConstructors().length == 0)) {
			classes.add(clazz);
			return classes;
		}
		if(clazz.isAssignableFrom(Set.class)){
			clazz =  HashSet.class;
			classes.add(clazz);
		} else if(clazz.isAssignableFrom(Comparator.class)){
			clazz = new Comparator() {
				public int compare(Object o1, Object o2) {
					return 0;
				}
			}.getClass();
			classes.add(clazz);
		} else if(clazz.isAssignableFrom(List.class)){
			clazz = ArrayList.class;
			classes.add(clazz);
		} else if(clazz.isAssignableFrom(Map.class)){
			clazz = HashMap.class;
			classes.add(clazz);
		} else {
			try {
				clazz = CallChecker.currentClassLoader.loadClass(clazz.getCanonicalName() + "Impl");
				classes.add(clazz);
			} catch (Throwable e) {
				ClassLoader classLoader = CallChecker.currentClassLoader;
				if(classLoader == null) {
					classes.add(clazz);
					return classes;
				}
				Class<?> CL_class = classLoader.getClass();
				while (CL_class != java.lang.ClassLoader.class) {
					CL_class = CL_class.getSuperclass();
				}
				Field ClassLoader_classes_field;
				try {
					ClassLoader_classes_field = CL_class.getDeclaredField("classes");
				} catch (NoSuchFieldException e1) {
					classes.add(clazz);
					return classes;
				}
				ClassLoader_classes_field.setAccessible(true);
				Vector<Class<?>> clazzes;
				try {
					clazzes = (Vector<Class<?>>) ClassLoader_classes_field.get(classLoader);
				} catch (IllegalAccessException e1) {
					classes.add(clazz);
					return classes;
				}
				for (int i = 0; i < clazzes.size(); i++) {
					Class<?> cl = clazzes.elementAt(i);
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
						classes.add(clazz);
					}
				}
				
			}

		}
		return classes;
	}

	@Override
	public int hashCode() {
		return  this.getClass().getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		return this.getClass().getSimpleName().equals(
				obj.getClass().getSimpleName());
	}

	@Override
	public int compareTo(Strategy o) {
		if(o == null) {
			return 1;
		}
		return toString().compareTo(o.toString());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
