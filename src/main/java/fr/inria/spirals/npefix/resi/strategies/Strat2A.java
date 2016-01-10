package fr.inria.spirals.npefix.resi.strategies;

/**
 * new A.foo
 * @author bcornu
 *
 */
public class Strat2A extends Strat2 {

	@Override
	public boolean isCompatibleAction(ACTION action) {
		return action.equals(ACTION.isCalled);
	}
}
