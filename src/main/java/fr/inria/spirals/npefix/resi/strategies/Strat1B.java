package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.Strategy;
import fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError;
/**
 * a=b
 * @author bcornu
 *
 */
public class Strat1B extends Strategy{

	@Override
	public <T> T beforeCalled(T o, Class<?> clazz) {
		if(o == null)
			o = obtain(clazz);
		return (T) o;
	}

	@Override
	public boolean collectData() {
		return true;
	}

	public <T> T isCalled(T o, Class<?> clazz) {
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
