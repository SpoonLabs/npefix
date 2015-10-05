package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.Strategy;
/**
 * b.foo
 * @author bcornu
 *
 */
public class Strat1A extends Strategy{
	
	
//	public static void fr.inria.spirals.npefix.main(String[] args) throws IllegalArgumentException, IllegalAccessException {
//		Class c = Strat1A.class;
//		Method m = c.getDeclaredMethods()[0];
//		Class mR = Method.class;
//		for (Field f : mR.getDeclaredFields()) {
//			f.setAccessible(true);
//			System.out.println(f.getGenericType() +" "+f.getName() + " = " + f.get(m));
//		}
//		
//		
//	}

	public <T> T isCalled(T o, Class clazz) {
		if(o==null)
			return obtain(clazz);
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
