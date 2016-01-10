package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;

import java.util.Collections;
import java.util.List;

public class NoStrat extends AbstractStrategy {

	@Override
	public boolean isCompatibleAction(ACTION action) {
		return false;
	}

	@Override
	public <T> List<Decision<T>> getSearchSpace(Class<T> clazz,
			Location location) {
		return Collections.EMPTY_LIST;
	}
}
