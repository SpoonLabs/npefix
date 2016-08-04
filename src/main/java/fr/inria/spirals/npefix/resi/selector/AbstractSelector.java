package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.oracle.AbstractOracle;
import fr.inria.spirals.npefix.resi.oracle.Oracle;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by thomas on 09/11/15.
 */
public abstract class AbstractSelector implements Selector {

	private Lapse currentLapse;
	private List<Lapse> lapses = new ArrayList<>();

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

	@Override
	public boolean startLaps(Lapse lapse) throws RemoteException {
		this.currentLapse = lapse;
		CallChecker.currentLapse = lapse;
		return false;
	}

	public Lapse getCurrentLapse() {
		return currentLapse;
	}

	public List<Strategy> getAllStrategies() {
		return Arrays.asList(strategies);
	}

	@Override
	public List<Lapse> getLapses() {
		return lapses;
	}

	@Override
	public boolean restartTest(Lapse lapse) {
		return false;
	}

	public Oracle createOracle(String m, boolean isValid) throws RemoteException {
		return new AbstractOracle(m, isValid);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
