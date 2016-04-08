package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.instance.Instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * b.foo
 * @author bcornu
 *
 */
public abstract class Strat1 extends AbstractStrategy {

	@Override
	public boolean collectData() {
		return true;
	}

	@Override
	public <T> List<Decision<T>> getSearchSpace(Class<T> clazz, Location location) {
		List<Decision<T>> output = new ArrayList<>();
		Map<String, Instance<T>> instances = obtain(clazz);
		Set<String> strings = instances.keySet();
		for (Iterator<String> iterator = strings.iterator(); iterator
				.hasNext(); ) {
			String key = iterator.next();
			Decision<T> decision = new Decision<>(this, location, instances.get(key), clazz);
			output.add(decision);
		}
		return output;
	}
}
