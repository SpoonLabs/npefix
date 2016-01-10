package fr.inria.spirals.npefix.resi.strategies;

/**
 * b.foo
 * @author bcornu
 *
 */
public class Strat1A extends Strat1 {
	@Override
	public boolean isCompatibleAction(ACTION action) {
		return action.equals(ACTION.isCalled);
	}
}
