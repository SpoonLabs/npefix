package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.ExceptionStack;
import fr.inria.spirals.npefix.resi.Strategy;
import fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError;

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
			return initNotNull(clazz);
		}
		return o;
	}

	public boolean beforeDeref(Object called) {
		return true;
	}


	@Override
	public <T> T returned(Class<?> clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
