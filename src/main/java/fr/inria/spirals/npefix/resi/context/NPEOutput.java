package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NPEOutput extends ArrayList<NPEFixExecution>{

	public NPEOutput() {

	}
	public Set<String> getTests() {
		Set<String> output = new HashSet<>();
		for (int i = 0; i < this.size(); i++) {
			NPEFixExecution npeFixExecution = this.get(i);
			output.add(npeFixExecution.getTest().getDeclaringClass().getCanonicalName() + "#" + npeFixExecution.getTest().getName());
		}
		return output;
	}

	public Set<Strategy> getRunnedStrategies() {
		Set<Strategy> output = new HashSet<>();
		for (int i = 0; i < this.size(); i++) {
			NPEFixExecution npeFixExecution = this.get(i);
			for (int j = 0; j < npeFixExecution.getDecisions().size(); j++) {
				fr.inria.spirals.npefix.resi.context.Decision decision = npeFixExecution.getDecisions().get(j);
				output.add(decision.getStrategy());
			}
		}
		return output;
	}

	public NPEOutput getExecutionsForStrategy(Strategy strategy) {
		NPEOutput output = new NPEOutput();
		for (int i = 0; i < this.size(); i++) {
			NPEFixExecution npeFixExecution = this.get(i);
			for (int j = 0; j < npeFixExecution.getDecisions().size(); j++) {
				fr.inria.spirals.npefix.resi.context.Decision decision = npeFixExecution.getDecisions().get(j);
				if(decision.getStrategy().equals(strategy)) {
					output.add(npeFixExecution);
				}
			}
		}
		return output;
	}

	public int getFailureCount() {
		int output = 0;
		for (int i = 0; i < this.size(); i++) {
			NPEFixExecution npeFixExecution = this.get(i);
			output += npeFixExecution.getTestResult().getFailureCount();
		}
		return output;
	}

	public int getFailureCount(Strategy strategy) {
		int output = 0;
		NPEOutput executionsForStrategy = getExecutionsForStrategy(strategy);
		for (int i = 0; i < executionsForStrategy.size(); i++) {
			NPEFixExecution npeFixExecution = executionsForStrategy.get(i);
			output += npeFixExecution.getTestResult().getFailureCount();
		}
		return output;
	}

	public NPEOutput getExecutionsForLocation(
			fr.inria.spirals.npefix.resi.context.Location location) {
		NPEOutput output = new NPEOutput();
		for (int i = 0; i < this.size(); i++) {
			NPEFixExecution npeFixExecution = this.get(i);
			if(npeFixExecution.getLocations().contains(location)) {
				output.add(npeFixExecution);
			}
		}
		return output;
	}

	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		output.put("date", new Date());
		for (int i = 0; i < this.size(); i++) {
			NPEFixExecution npeFixExecution = this.get(i);
			output.append("executions", npeFixExecution.toJSON());
		}
		return output;
	}
}
