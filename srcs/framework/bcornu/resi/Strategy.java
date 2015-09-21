package bcornu.resi;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Strategy {

	public abstract <T> T isCalled(T o, Class clazz);

	public abstract <T> T returned(Class clazz);
	
	public abstract boolean beforeDeref(Object called);

	protected <T> T obtain(Class clazz) {
		if(clazz==null)
			return null;
		List<Object> vars = CallChecker.getCurrentVars();
		for (Object object : vars) {
			if(object!=null && object.getClass()!=null && object.getClass().isAssignableFrom(clazz)){
				System.out.println("var found!");
				return (T) object;
			}
		}
		throw new AbnormalExecutionError("cannot found var: "+clazz);
	}
	
	public <T> T init(Class clazz) {
		if(clazz==null)
			return null;
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}
		return null;
	}
	
	protected <T> T  initPrimitive(Class clazz){
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

	protected <T> T  initNotNull(Class clazz){
		if(clazz==null)
			return null;
		if(clazz.isPrimitive()){
			return initPrimitive(clazz);
		}
		if(clazz.isInterface()){
			if(clazz.isAssignableFrom(Set.class)){
				return (T) new HashSet();
			}
			if(clazz.isAssignableFrom(Comparator.class)){
				return (T) new Comparator() {
					public int compare(Object o1, Object o2) {
						return 0;
					}
				};
			}
			else throw new AbnormalExecutionError("missing interface"+clazz);
		}
		Object res = null;
		try {
			res = (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			System.err.println("cannot new instance "+clazz);
			try{
				for (Constructor constructor : clazz.getConstructors()) {
					try{
						Class[] types = constructor.getParameterTypes();
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
	
}
