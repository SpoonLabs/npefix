package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.NPEFixExecution;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.util.List;
import java.util.Set;

public interface Selector {

	<T> Decision<T> select(List<Decision<T>> decisions);

	boolean restartTest(NPEFixExecution npeFixExecution);

	List<Strategy> getStrategies();

	Set<Decision> getSearchSpace();
}
