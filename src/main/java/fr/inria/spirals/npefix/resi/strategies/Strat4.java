package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.Strategy;
import fr.inria.spirals.npefix.resi.exception.ForceReturn;
import fr.inria.spirals.npefix.resi.exception.ReturnNotSupported;
/**
 * return null
 * @author bcornu
 *
 */
public class Strat4 extends Strategy {
	
	private ReturnType rt;
	
	public Strat4(ReturnType rt) {
		this.rt=rt;
	}

	@Override
	public boolean collectData() {
		return rt.equals(ReturnType.VAR);
	}

	public <T> T isCalled(T o, Class<?> clazz) {
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
	public <T> T returned(Class<?> clazz) {
		switch (rt) {
		case NULL:
			//System.out.println("return null");
			return null;
		case NEW:
			//System.out.println("return new");
			return initNotNull(clazz);
		case VAR:
			//System.out.println("return var");
			return obtain(clazz);
		default:
			throw new ReturnNotSupported(clazz.getCanonicalName());
		}
	}

	@Override
	public int hashCode() {
		return this.getClass().getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.getClass().getSimpleName().equals(
				obj.getClass().getSimpleName()) && rt.equals(((Strat4)obj).rt);
	}

	@Override
	public String toString() {
		return super.toString() + " " + rt.name();
	}
}
