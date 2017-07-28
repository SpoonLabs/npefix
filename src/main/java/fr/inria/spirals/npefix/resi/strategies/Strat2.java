package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.context.instance.Instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * new A.foo
 * @author bcornu
 *
 */
public abstract class Strat2 extends AbstractStrategy {

	@Override
	public <T> List<Decision<T>> getSearchSpace(Object value,
			Class<T> clazz,
			Location location,
			MethodContext context) {
		List<Decision<T>> output = new ArrayList<>();
		List<Instance<T>> instances = initNotNull(clazz);
		Collections.sort(instances);
		for (int i = 0; i < instances.size(); i++) {
			Instance<T> instance = instances.get(i);
			Decision<T> decision = new Decision<>(this, location, instance, clazz);
			output.add(decision);
		}
		return output;
	}
}
