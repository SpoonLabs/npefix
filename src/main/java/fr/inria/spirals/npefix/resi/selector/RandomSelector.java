package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.RandomGenerator;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RandomSelector extends AbstractSelector {

	private Set<Decision> decisions = new HashSet<>();

	@Override
	public boolean startLaps(Lapse lapse) throws RemoteException {
		super.startLaps(lapse);
		return true;
	}

	@Override
	public List<Strategy> getStrategies() {
		ArrayList<Strategy> strategies = new ArrayList<>(getAllStrategies());
		strategies.remove(new NoStrat());
		return strategies;
	}

	/**
	 * Select randomly a strategy
	 * @return a strategy
	 * @param decisions
	 */
	@Override
	public <T> Decision<T> select(List<Decision<T>> decisions) {
		this.decisions.addAll(decisions);
		int maxValue = decisions.size();
		return decisions.get(RandomGenerator.nextInt(1, maxValue));
	}

	@Override
	public boolean restartTest(Lapse lapse) {
		super.restartTest(lapse);
		return false;
		//return !laps.getOracle().wasSuccessful();
	}

	@Override
	public Set<Decision> getSearchSpace() {
		return decisions;
	}
}
