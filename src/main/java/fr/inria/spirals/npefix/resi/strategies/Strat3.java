package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.Strategy;
import fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError;
/**
 * if != null
 * @author bcornu
 *
 */
public class Strat3 extends Strategy{

	public <T> T isCalled(T o, Class<?> clazz) {
		return o;
	}

	public boolean beforeDeref(Object called) {
		return called!=null;
	}

	@Override
	public <T> T returned(Class<?> clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
