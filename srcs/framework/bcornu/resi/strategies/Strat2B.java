package bcornu.resi.strategies;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import bcornu.resi.AbnormalExecutionError;
import bcornu.resi.ExceptionStack;
import bcornu.resi.ForceReturn;
import bcornu.resi.Strategy;
/**
 * a=new A
 * @author bcornu
 *
 */
public class Strat2B extends Strategy{

	public <T> T isCalled(T o, Class clazz) {
		if (o == null) {
			if (ExceptionStack.isStoppable(NullPointerException.class)) {
				return null;
			}
			if(clazz.isPrimitive()){
				o = initPrimitive(clazz);
				return o;
			}
			if(clazz.isInterface()){
				if(clazz.isAssignableFrom(Set.class)){
					o= (T) new HashSet();
				}else
				if(clazz.isAssignableFrom(Comparator.class)){
					o= (T) new Comparator() {
						public int compare(Object o1, Object o2) {
							return 0;
						}
					};
				}
				else throw new AbnormalExecutionError("missing interface"+clazz);
			}
			try {
				o = (T) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				System.err.println("cannot new instance "+clazz);
				try{
					for (Constructor constructor : clazz.getConstructors()) {
						try{
							Class[] types = constructor.getParameterTypes();
							Object[] params = new Object[types.length];
							for (int i = 0; i < types.length; i++) {
								try{
									if(types[i].equals(clazz))
										throw new ForceReturn();
									else
										params[i] = init(types[i]);
								}catch (Throwable t){
									t.printStackTrace();
								}
							}
							o = (T) constructor.newInstance(params);
							return (T) o;
						}catch (Throwable t){
							t.printStackTrace();
						}
					}
				}catch (Throwable t){
					t.printStackTrace();
				}
			}
		}
		return (T) o;
	}

	public boolean beforeDeref(Object called) {
		return true;
	}


	@Override
	public <T> T returned(Class clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
