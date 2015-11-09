package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.Strategy;
import fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError;

public class NoStrat extends Strategy{

	@Override
	public <T> T init(Class<?> clazz) {
		if(clazz.isPrimitive()) {
			return initPrimitive(clazz);
		}
		return null;
	}



	public <T> T isCalled(T o, Class<?> clazz) {
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
