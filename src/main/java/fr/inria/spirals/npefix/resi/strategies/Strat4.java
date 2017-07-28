package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.ConstructorContext;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.context.instance.PrimitiveInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * return null
 * @author bcornu
 *
 */
public class Strat4 extends AbstractStrategy {

	private ReturnType returnType;

	public Strat4(ReturnType returnType) {
		this.returnType = returnType;
	}

	@Override
	public boolean collectData() {
		return returnType.equals(ReturnType.VAR);
	}


	@Override
	public boolean isCompatibleAction(ACTION action) {
		return action.equals(ACTION.isCalled) || action.equals(ACTION.beforeDeref) || action.equals(ACTION.tryRepair);
	}

	@Override
	public <T> List<Decision<T>> getSearchSpace(Object value,
			Class<T> clazz,
			Location location,
			MethodContext context) {
		clazz = context.getMethodType();

		List<Decision<T>> output = new ArrayList<>();
		if (context instanceof ConstructorContext) {
			// constructor don't have expected return
			if (returnType == ReturnType.VOID) {
				output.add(new Decision(this, location, new PrimitiveInstance(null)));
			}
			return output;
		}
		if (clazz == null) {
			return output;
		}
		switch (returnType) {
		case VOID:
			if(void.class.equals(clazz)) {
				output.add(new Decision(this, location, new PrimitiveInstance(null), void.class));
			}
			break;
		case NULL:
			if(!clazz.isPrimitive()) {
				output.add(new Decision(this, location, new PrimitiveInstance(null), clazz));
			}
			break;
		case NEW:
			List<Instance<T>> instances = initNotNull(clazz);
			for (int i = 0; i < instances.size(); i++) {
				Instance<T> instance = instances.get(i);
				Decision<T> decision = new Decision<>(this, location, instance, clazz);
				output.add(decision);
			}
			break;
		case VAR:
			Map<String, Instance<T>> variables = obtain(clazz);
			Set<String> strings = variables.keySet();
			for (String key : strings) {
				Decision<T> decision = new Decision<>(this, location,
						variables.get(key), clazz);
				output.add(decision);
			}
			break;
		}
		return output;
	}

	public ReturnType getReturnType() {
		return returnType;
	}

	@Override
	public int hashCode() {
		return this.getClass().getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && returnType.equals(((Strat4)obj).returnType);
	}

	@Override
	public String toString() {
		return super.toString() + " " + returnType.name();
	}
}
