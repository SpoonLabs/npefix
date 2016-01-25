package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NPEOutput extends ArrayList<Laps>{

	public NPEOutput() {

	}
	public Set<String> getTests() {
		Set<String> output = new HashSet<>();
		for (int i = 0; i < this.size(); i++) {
			Laps laps = this.get(i);
			output.add(laps.getTestClassName() + "#" + laps.getTestName());
		}
		return output;
	}

	public Set<Strategy> getRunnedStrategies() {
		Set<Strategy> output = new HashSet<>();
		for (int i = 0; i < this.size(); i++) {
			Laps laps = this.get(i);
			for (int j = 0; j < laps.getDecisions().size(); j++) {
				Decision decision = laps.getDecisions().get(j);
				output.add(decision.getStrategy());
			}
		}
		return output;
	}

	public NPEOutput getExecutionsForStrategy(Strategy strategy) {
		NPEOutput output = new NPEOutput();
		for (int i = 0; i < this.size(); i++) {
			Laps laps = this.get(i);
			for (int j = 0; j < laps.getDecisions().size(); j++) {
				Decision decision = laps.getDecisions().get(j);
				if(decision.getStrategy().equals(strategy)) {
					output.add(laps);
				}
			}
		}
		return output;
	}

	public int getFailureCount() {
		int output = 0;
		for (int i = 0; i < this.size(); i++) {
			Laps laps = this.get(i);
			if(!laps.getOracle().isValid()) {
				output += 1;
			}
		}
		return output;
	}

	public int getFailureCount(Strategy strategy) {
		int output = 0;
		NPEOutput executionsForStrategy = getExecutionsForStrategy(strategy);
		for (int i = 0; i < executionsForStrategy.size(); i++) {
			Laps laps = executionsForStrategy.get(i);
			if(!laps.getOracle().isValid()) {
				output += 1;
			}
		}
		return output;
	}

	public NPEOutput getExecutionsForLocation(Location location) {
		NPEOutput output = new NPEOutput();
		for (int i = 0; i < this.size(); i++) {
			Laps laps = this.get(i);
			if(laps.getLocations().contains(location)) {
				output.add(laps);
			}
		}
		return output;
	}

	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("date", new Date());
		for (int i = 0; i < this.size(); i++) {
			Laps laps = this.get(i);
			output.append("executions", laps.toJSON());
		}
		return output;
	}
}
