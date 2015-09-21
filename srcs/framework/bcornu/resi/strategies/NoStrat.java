package bcornu.resi.strategies;

import bcornu.resi.AbnormalExecutionError;
import bcornu.resi.Strategy;

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
