package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.ForceReturn;
import fr.inria.spirals.npefix.resi.Strategy;
/**
 * return null
 * @author bcornu
 *
 */
public class Strat4 extends Strategy{
	
	private ReturnType rt;
	
	public Strat4(ReturnType rt) {
		this.rt=rt;
	}

	public <T> T isCalled(T o, Class clazz) {
		if(o==null)
			throw new ForceReturn();
		return o;
	}

	public boolean beforeDeref(Object called) {
		if(called==null)
			throw new ForceReturn();
		return true;
	}

	@Override
	public <T> T returned(Class clazz) {
		switch (rt) {
		case NULL:
			System.out.println("return null");
			return null;
		case NEW:
			System.out.println("return new");
			return initNotNull(clazz);
		case VAR:
			System.out.println("return var");
			return obtain(clazz);
		default:
			throw new AbnormalExecutionError();
		}
	}
}
