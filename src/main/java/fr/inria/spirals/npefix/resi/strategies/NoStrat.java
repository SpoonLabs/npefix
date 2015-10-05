package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.Strategy;

public class NoStrat extends Strategy{

	public <T> T isCalled(T o, Class clazz) {
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
