package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.NPEFixExecution;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	public boolean restartTest(NPEFixExecution npeFixExecution) {
		Set<Location> locations = npeFixExecution.getLocations();
		Map<Decision, Integer> indexes = npeFixExecution.getCurrentIndex();

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
