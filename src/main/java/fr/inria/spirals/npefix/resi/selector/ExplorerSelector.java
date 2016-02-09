package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Laps;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ExplorerSelector extends AbstractSelector {

	private Set<List<Decision>> usedDecisionSeq = new HashSet<>();
	private Map<Location, Set<Decision>> decisions = new HashMap<>();
	private Map<String,Stack<Decision>> stackDecision  = new HashMap<>();
	private Laps currentLaps = null;
	private String currentTestKey;

	@Override
	public boolean startLaps(Laps laps) throws RemoteException {
		currentLaps = laps;
		this.currentTestKey = currentLaps.getTestClassName() + "#" + currentLaps.getTestName();
		if(!stackDecision.containsKey(currentTestKey)) {
			stackDecision.put(currentTestKey, new Stack<Decision>());
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
	public <T> Decision<T> select(List<Decision<T>> decisions) {
		try {
			initDecision(decisions);

			for (Iterator<Decision> iterator = stackDecision.get(currentTestKey)
					.iterator(); iterator
						 .hasNext(); ) {
				Decision decision = iterator.next();
				if (decisions.contains(decision)) {
					return decision;
				}
			}

			CallChecker.currentLaps.putMetadata("strategy_selection", "exploration");

			List<Decision> otherDecision = new ArrayList<>();
			otherDecision.addAll(stackDecision.get(currentTestKey));

			for (Decision decision : decisions) {
				otherDecision.add(decision);
				if (!usedDecisionSeq.contains(otherDecision)) {
					decision.setUsed(true);
					decision.setDecisionType("new");
					stackDecision.get(currentTestKey).push(decision);
					return decision;
				}
				otherDecision.remove(decision);
			}
			return null;
		} catch (Throwable e) {
			e.printStackTrace();
			throw  e;
		}
	}

	@Override
	public boolean restartTest(Laps laps) {
		laps.setEndDate(new Date());
		if(laps.getDecisions().isEmpty()) {
			return false;
		}
		getLapses().add(laps);

		usedDecisionSeq.add(laps.getDecisions());

		Decision lastDecision = stackDecision.get(currentTestKey).pop();

		List<Decision> otherDecision = new ArrayList<>();
		otherDecision.addAll(stackDecision.get(currentTestKey));

		Set<Decision> decisions = this.decisions.get(lastDecision.getLocation());
		for (Decision decision : decisions) {
			otherDecision.add(decision);
			if (!usedDecisionSeq.contains(otherDecision)) {
				return false;
			}
			otherDecision.remove(decision);
		}
		if(!stackDecision.get(currentTestKey).isEmpty()) {
			stackDecision.get(currentTestKey).pop();
		}
		return false;
	}
}
