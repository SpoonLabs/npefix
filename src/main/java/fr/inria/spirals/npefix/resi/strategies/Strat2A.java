package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.ExceptionStack;
import fr.inria.spirals.npefix.resi.ForceReturn;
import fr.inria.spirals.npefix.resi.Strategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * new A.foo
 * @author bcornu
 *
 */
public class Strat2A extends Strategy{

	public <T> T isCalled(T o, Class<?> clazz) {
		if (o == null) {
			if (ExceptionStack.isStoppable(NullPointerException.class)) {
				return null;
			}
			if(clazz.equals(Class.class)) {
				return null;
			}
			
			if(clazz.isPrimitive()){
				return initPrimitive(clazz);
			}
			if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) ) {
				clazz = getImplForInterface(clazz);
				if(clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) ) {
					throw new AbnormalExecutionError("missing interface " + clazz);
				}
			}
			Object res = null;
			try {
				res = (T) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {

				try{
					Constructor[] consts = clazz.getConstructors();
					for (Constructor constructor : consts) {
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
							res = (T) constructor.newInstance(params);
							return (T) res;
						}catch (Throwable t){
							t.printStackTrace();
						}
					}
				}catch (Throwable t){
					t.printStackTrace();
				}
				System.err.println("cannot new instance "+clazz);
			}
			return (T) res;
		}
		return (T) o;
	}

	public boolean beforeDeref(Object called) {
		return true;
	}


	@Override
	public <T> T returned(Class<?> clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
