package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.context.Lapse;
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
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 09/11/15.
 */
public abstract class AbstractSelector implements Selector {

	private Lapse currentLapse;
	private List<Lapse> lapses = new ArrayList<>();

	protected static final Strategy[] strategies = new Strategy[]{
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
		lapse.setFinished(false);
		return false;
	}

	@Override
	public Lapse getCurrentLapse() {
		return currentLapse;
	}

	@Override
	public Lapse updateCurrentLapse(Lapse updatedLapse) {
		if (currentLapse == null || currentLapse.equals(updatedLapse)) {
			currentLapse = updatedLapse;
		}
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
		lapse.setFinished(true);
		if (!lapse.getDecisions().isEmpty()) {
			lapses.add(lapse);
		}
		lapse.setEndDate(new Date());
		return false;
	}

	@Override
	public void reset() throws RemoteException {
		currentLapse = null;
		lapses = new ArrayList<>();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
