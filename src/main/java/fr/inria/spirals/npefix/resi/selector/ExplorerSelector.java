package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.exception.NoMoreDecision;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ExplorerSelector extends AbstractSelector {

	private Map<String, Set<List<Decision>>> usedDecisionSeq = new HashMap<>();
	private Map<Location, Set<Decision>> decisions = new HashMap<>();
	private Map<String, Stack<Decision>> stackDecision  = new HashMap<>();
	private String currentTestKey;
	private List<Strategy> strategies;

	public ExplorerSelector() {
		this(AbstractSelector.strategies);
	}

	public ExplorerSelector(Strategy... strategies) {
		this.strategies = new ArrayList<>(Arrays.asList(strategies));
		this.strategies.remove(new NoStrat());
	}

	@Override
	public boolean startLaps(Lapse lapse) throws RemoteException {
		super.startLaps(lapse);
		this.currentTestKey = getCurrentLapse().getTestClassName() + "#" + getCurrentLapse().getTestName();
		if(!stackDecision.containsKey(currentTestKey)) {
			stackDecision.put(currentTestKey, new Stack<Decision>());
		}
		if(!usedDecisionSeq.containsKey(currentTestKey)) {
			usedDecisionSeq.put(currentTestKey, new HashSet<List<Decision>>());
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

			for (Decision decision : stackDecision.get(currentTestKey)) {
				if (decisions.contains(decision)) {
					return decision;
				}
			}

			getCurrentLapse().putMetadata("strategy_selection", "exploration");

			List<Decision> otherDecision = new ArrayList<>();
			otherDecision.addAll(stackDecision.get(currentTestKey));

			for (Decision decision : decisions) {
				otherDecision.add(decision);
				if (!usedDecisionSeq.get(currentTestKey).contains(otherDecision)) {
					decision.setUsed(true);
					decision.setDecisionType(Decision.DecisionType.NEW);
					stackDecision.get(currentTestKey).push(decision);
					return decision;
				}
				otherDecision.remove(decision);
			}
			throw new NoMoreDecision();
		} catch (Throwable e) {
			if (!(e instanceof NoMoreDecision)) {
				e.printStackTrace();
			}
			throw  e;
		}
	}

	@Override
	public boolean restartTest(Lapse lapse) {
		super.restartTest(lapse);
		if(lapse.getDecisions().isEmpty()) {
			return false;
		}
		usedDecisionSeq.get(currentTestKey).add(lapse.getDecisions());

		stackDecision.put(currentTestKey, new Stack<Decision>());
		for (int i = 0; i < lapse.getDecisions().size(); i++) {
			Decision decision = lapse.getDecisions().get(i);
			stackDecision.get(currentTestKey).add(decision);
		}

		Decision lastDecision = stackDecision.get(currentTestKey).pop();

		List<Decision> otherDecision = new ArrayList<>();
		otherDecision.addAll(stackDecision.get(currentTestKey));

		Set<Decision> decisions = this.decisions.get(lastDecision.getLocation());
		for (Decision decision : decisions) {
			otherDecision.add(decision);
			if (!usedDecisionSeq.get(currentTestKey).contains(otherDecision)) {
				return false;
			}
			otherDecision.remove(decision);
		}
		if (!stackDecision.get(currentTestKey).isEmpty()) {
			stackDecision.get(currentTestKey).pop();
		}
		return false;
	}

	@Override
	public void reset() throws RemoteException {
		super.reset();
		stackDecision = new HashMap<>();
		usedDecisionSeq = new HashMap<>();
		decisions = new HashMap<>();
	}
}
