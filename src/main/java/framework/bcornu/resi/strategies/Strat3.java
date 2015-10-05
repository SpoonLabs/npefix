package framework.bcornu.resi.strategies;

import framework.bcornu.resi.AbnormalExecutionError;
import framework.bcornu.resi.Strategy;
/**
 * if != null
 * @author bcornu
 *
 */
public class Strat3 extends Strategy{

	public <T> T isCalled(T o, Class clazz) {
		return o;
	}

	public boolean beforeDeref(Object called) {
		return called!=null;
	}

	@Override
	public <T> T returned(Class clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
