package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.MethodContext;

import java.util.Collections;
import java.util.List;

public class NoStrat extends AbstractStrategy {

	@Override
	public boolean isCompatibleAction(ACTION action) {
		return false;
	}

	@Override
	public <T> List<Decision<T>> getSearchSpace(Object value,
			Class<T> clazz,
			Location location,
			MethodContext context) {
		return Collections.EMPTY_LIST;
	}
}
