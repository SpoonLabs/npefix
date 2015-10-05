package framework.bcornu.resi.strategies;

import framework.bcornu.resi.AbnormalExecutionError;
import framework.bcornu.resi.Strategy;
/**
 * a=b
 * @author bcornu
 *
 */
public class Strat1B extends Strategy{

	public <T> T isCalled(T o, Class clazz) {
		if(o==null)
			o = obtain(clazz);
		return (T) o;}

	public boolean beforeDeref(Object called) {
		return true;
	}


	@Override
	public <T> T returned(Class clazz) {
		throw new AbnormalExecutionError("should not call return");
	}
}
