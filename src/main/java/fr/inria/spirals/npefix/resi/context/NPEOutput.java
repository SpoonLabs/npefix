package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.json.JSONObject;
import spoon.Launcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NPEOutput extends ArrayList<Lapse>{

	public NPEOutput() {

	}
	public Set<String> getTests() {
		Set<String> output = new HashSet<>();
		for (int i = 0; i < this.size(); i++) {
			Lapse lapse = this.get(i);
			output.add(lapse.getTestClassName() + "#" + lapse.getTestName());
		}
		return output;
	}

	public Set<Strategy> getRanStrategies() {
		Set<Strategy> output = new HashSet<>();
		for (int i = 0; i < this.size(); i++) {
			Lapse lapse = this.get(i);
			for (int j = 0; j < lapse.getDecisions().size(); j++) {
				Decision decision = lapse.getDecisions().get(j);
				output.add(decision.getStrategy());
			}
		}
		return output;
	}

	public NPEOutput getExecutionsForStrategy(Strategy strategy) {
		NPEOutput output = new NPEOutput();
		for (int i = 0; i < this.size(); i++) {
			Lapse lapse = this.get(i);
			for (int j = 0; j < lapse.getDecisions().size(); j++) {
				Decision decision = lapse.getDecisions().get(j);
				if(decision.getStrategy().equals(strategy)) {
					output.add(lapse);
				}
			}
		}
		return output;
	}

	public int getFailureCount() {
		int output = 0;
		for (int i = 0; i < this.size(); i++) {
			Lapse lapse = this.get(i);
			if(!lapse.getOracle().isValid()) {
				output += 1;
			}
		}
		return output;
	}

	public int getFailureCount(Strategy strategy) {
		int output = 0;
		NPEOutput executionsForStrategy = getExecutionsForStrategy(strategy);
		for (int i = 0; i < executionsForStrategy.size(); i++) {
			Lapse lapse = executionsForStrategy.get(i);
			if(!lapse.getOracle().isValid()) {
				output += 1;
			}
		}
		return output;
	}

	public NPEOutput getExecutionsForLocation(Location location) {
		NPEOutput output = new NPEOutput();
		for (int i = 0; i < this.size(); i++) {
			Lapse lapse = this.get(i);
			if(lapse.getLocations().contains(location)) {
				output.add(lapse);
			}
		}
		return output;
	}

	public JSONObject toJSON(Launcher spoon) {
		JSONObject output = new JSONObject();
		output.put("date", new Date());
		for (int i = 0; i < this.size(); i++) {
			Lapse lapse = this.get(i);
			output.append("executions", lapse.toJSON(spoon));
		}
		return output;
	}
}
