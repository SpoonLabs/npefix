package fr.inria.spirals.npefix.resi.strategies;

/**
 * a=b
 * @author bcornu
 *
 */
public class Strat1B extends Strat1 {

	@Override
	public boolean isCompatibleAction(ACTION action) {
		return action.equals(ACTION.beforeCalled);
	}
}
