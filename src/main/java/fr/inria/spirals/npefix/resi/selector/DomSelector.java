package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Laps;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DomSelector extends AbstractSelector {

	public static Strategy strategy = new Strat3();
	public int currentIndex = 0;
	private Set<Decision> decisions = new HashSet<>();

	@Override
	public <T> Decision<T> select(List<Decision<T>> decisions) {
		this.decisions.addAll(decisions);
		return decisions.get(currentIndex);
	}

	@Override
	public boolean restartTest(Laps laps) {
		if(currentIndex < decisions.size() - 1) {
			return true;
		}
		return false;
	}

	@Override
	public List<Strategy> getStrategies() {
		return Arrays.asList(strategy);
	}

	@Override
	public Set<Decision> getSearchSpace() {
		return decisions;
	}

	@Override
	public String toString() {
		return super.toString() + " " + strategy;
	}
}
