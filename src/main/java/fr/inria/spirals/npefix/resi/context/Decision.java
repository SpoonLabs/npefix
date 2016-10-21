package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.json.JSONObject;

import java.io.Serializable;

public class Decision<T> implements Serializable {
	public enum DecisionType {
		RANDOM,
		NEW,
		BEST
	}
	private Strategy strategy;
	private Location location;
	private Instance<T> value;
	private String valueType;
	private DecisionType decisionType;
	private int nbUse = 0;
	private boolean isUsed = false;
	private double epsilon;

	public Decision(Strategy strategy, Location location) {
		this.strategy = strategy;
		this.location = location;
		this.decisionType = DecisionType.RANDOM;
	}

	public Decision(Strategy strategy, Location location, Instance<T> value) {
		this(strategy, location);
		this.value = value;
	}

	public Decision(Strategy strategy, Location location, Instance<T> value, Class<T> valueType) {
		this(strategy, location, value);
		this.valueType = valueType.getCanonicalName();
	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setUsed(boolean used) {
		isUsed = used;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public T getValue() {
		return value.getValue();
	}

	public Instance getInstance() {
		return value;
	}
	public void setValue(Instance<T> value) {
		this.value = value;
	}

	public Class<T> getValueType() {
		try {
			return (Class<T>) getClass().forName(valueType);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void setValueType(Class<T> valueType) {
		this.valueType = valueType.getCanonicalName();
	}

	public DecisionType getDecisionType() {
		return decisionType;
	}

	public void setDecisionType(DecisionType decisionType) {
		this.decisionType = decisionType;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public int getNbUse() {
		return nbUse;
	}

	public void setNbUse(int nbUse) {
		this.nbUse = nbUse;
	}

	public void increaseNbUse() {
		nbUse ++;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Decision<?> decision = (Decision<?>) o;

		if (!strategy.equals(decision.strategy))
			return false;
		if (!location.equals(decision.location))
			return false;

		if (valueType != null ?
				!valueType.equals(decision.valueType) :
				decision.valueType != null)
			return false;

		if (value != null ?
				!value.equals(decision.value) :
				decision.value != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return -1;
	}

	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("strategy", strategy.toString());
		output.put("decisionType", decisionType);
		output.put("used", isUsed);
		output.put("epsilon", epsilon);
		output.put("nbUse", nbUse);
		if (value == null) {
			System.out.println(strategy);
		} else {
			output.put("value", value.toJSON());
		}
		output.put("location", location.toJSON());
		return output;
	}

	@Override
	public String toString() {
		return strategy + " " + getLocation() + " " + (isUsed?"Used ":"") + value;
	}
}
