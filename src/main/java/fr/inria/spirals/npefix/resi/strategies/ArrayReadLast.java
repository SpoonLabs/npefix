package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.context.instance.ArrayReadInstance;
import fr.inria.spirals.npefix.resi.context.instance.Instance;

import java.util.ArrayList;
import java.util.List;

/**
 * Replace null element by existing one
 * b.foo
 * @author bcornu
 *
 */
public class ArrayReadLast extends AbstractStrategy {

	@Override
	public boolean collectData() {
		return true;
	}

	@Override
	public <T> List<Decision<T>> getSearchSpace(Object array, Class<T> clazz, Location location, MethodContext context) {
		List<Decision<T>> output = new ArrayList<>();
		Instance instance = new ArrayReadInstance("<?>", 0);
		output.add(new Decision<>(this, location, instance, clazz));
		return output;
	}

	@Override
	public boolean isCompatibleAction(ACTION action) {
		return action.equals(ACTION.arrayAccess);
	}
}
