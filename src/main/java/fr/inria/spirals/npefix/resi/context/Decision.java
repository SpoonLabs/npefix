package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.json.JSONObject;

public class Decision<T> {
	private Strategy strategy;
	private Location location;
	private Instance<T> value;
	private Class<T> valueType;
	private String variableName;
	private String decisionType;
	private boolean isUsed = false;

	public Decision(Strategy strategy, Location location) {
		this.strategy = strategy;
		this.location = location;
		this.decisionType = "random";
	}

	public Decision(Strategy strategy, Location location, Instance<T> value) {
		this(strategy, location);
		this.value = value;
	}

	public Decision(Strategy strategy, Location location, Instance<T> value, Class<T> valueType) {
		this(strategy, location, value);
		this.valueType = valueType;
	}

	public Decision(Strategy strategy, Location location, Instance<T> value, Class<T> valueType, String variableName) {
		this(strategy, location, value, valueType);
		this.variableName = variableName;
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

	public void setValue(Instance<T> value) {
		this.value = value;
	}

	public Class<T> getValueType() {
		return valueType;
	}

	public void setValueType(Class<T> valueType) {
		this.valueType = valueType;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public String getDecisionType() {
		return decisionType;
	}

	public void setDecisionType(String decisionType) {
		this.decisionType = decisionType;
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

		if (variableName != null ?
				!variableName.equals(decision.variableName) :
				decision.variableName != null)
			return false;
		else if(variableName != null){
			return true;
		}

		if(value == decision.value) {
			return true;
		}
		if (value != null ?
				!value.equals(decision.value) :
				decision.value != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = strategy.hashCode();
		result = 31 * result + location.hashCode();
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("strategy", strategy.toString());
		output.put("decisionType", decisionType);
		output.put("used", isUsed);

		JSONObject valueJSON = new JSONObject();
		if(value!= null) {
			valueJSON.put("value", value.toString());
		} else {
			valueJSON.put("value", "null");
		}

		valueJSON.put("variableName", variableName);
		valueJSON.put("type", valueType);
		output.put("value", valueJSON);

		JSONObject locationJSON = new JSONObject();
		locationJSON.put("class", location.className);
		locationJSON.put("line", location.line);
		locationJSON.put("sourceStart", location.sourceStart);
		locationJSON.put("sourceEnd", location.sourceEnd);

		output.put("location", locationJSON);
		return output;
	}

	@Override
	public String toString() {
		return strategy + " " + getLocation() + " " + (isUsed?"Used ":"") + value;
	}
}
