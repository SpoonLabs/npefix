package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.NPEFixExecution;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.util.Arrays;
import java.util.List;

/**
 * Created by thomas on 09/11/15.
 */
public abstract class AbstractSelector implements Selector {
	private static final Strategy[] strategies = new Strategy[]{
			new NoStrat(),
			new Strat1A(),
			new Strat1B(),
			new Strat2A(),
			new Strat2B(),
			new Strat3(),
			new Strat4(ReturnType.NULL),
			new Strat4(ReturnType.VAR),
			new Strat4(ReturnType.NEW),
			new Strat4(ReturnType.VOID)
	};

	public List<Strategy> getAllStrategies() {
		return Arrays.asList(strategies);
	}

	@Override
	public boolean restartTest(NPEFixExecution npeFixExecution) {
		return false;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
