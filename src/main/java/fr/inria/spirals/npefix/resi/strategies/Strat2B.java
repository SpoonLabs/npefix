package fr.inria.spirals.npefix.resi.strategies;

/**
 * a=new A
 * @author bcornu
 *
 */
public class Strat2B extends Strat2 {

	@Override
	public boolean isCompatibleAction(ACTION action) {
		return action.equals(ACTION.beforeCalled);
	}
}
