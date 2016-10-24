package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonoExplorerSelector extends AbstractSelector {

	private Map<String, Set<Decision>> usedDecisions = new HashMap<>();
	private Map<Location, Set<Decision>> decisions = new HashMap<>();
	private String currentTestKey;

	@Override
	public boolean startLaps(Lapse lapse) throws RemoteException {
		super.startLaps(lapse);
		this.currentTestKey = getCurrentLapse().getTestClassName() + "#" + getCurrentLapse().getTestName();
		if (!usedDecisions.containsKey(currentTestKey)) {
			usedDecisions.put(currentTestKey, new HashSet<Decision>());
		}
		return true;
	}

	private <T> void initDecision(List<Decision<T>> decisions) {
		for (int i = 0; i < decisions.size(); i++) {
			Decision decision = decisions.get(i);
			if(!this.decisions.containsKey(decision.getLocation())) {
				this.decisions.put(decision.getLocation(), new HashSet<Decision>());
			}
			if(!this.decisions.get(decision.getLocation()).contains(decision)) {
				this.decisions.get(decision.getLocation()).add(decision);
			}
		}
	}

	@Override
	public List<Strategy> getStrategies() {
		ArrayList<Strategy> strategies = new ArrayList<>(getAllStrategies());
		strategies.remove(new NoStrat());
		return strategies;
	}

	@Override
	public Set<Decision> getSearchSpace() {
		HashSet<Decision> decisions = new HashSet<>();
		Collection<Set<Decision>> values = this.decisions.values();
		for (Iterator<Set<Decision>> iterator = values.iterator(); iterator
				.hasNext(); ) {
			Set<Decision> decisionSet = iterator.next();
			decisions.addAll(decisionSet);
		}
		return decisions;
	}

	@Override
	public synchronized <T> Decision<T> select(List<Decision<T>> decisions) {
		try {
			initDecision(decisions);

			getCurrentLapse().putMetadata("strategy_selection", "exploration");

			for (Decision decision : decisions) {
				if (!usedDecisions.get(currentTestKey).contains(decision)) {
					decision.setDecisionType(Decision.DecisionType.NEW);
					return decision;
				}
			}
			throw new NoMoreDecisionException("No more available decision");
		} catch (Throwable e) {
			//e.printStackTrace();
			throw  e;
		}
	}

	@Override
	public boolean restartTest(Lapse lapse) {
		super.restartTest(lapse);
		if(lapse.getDecisions().isEmpty()) {
			return false;
		}
		usedDecisions.get(currentTestKey).add(lapse.getDecisions().get(0));
		return false;
	}
}
