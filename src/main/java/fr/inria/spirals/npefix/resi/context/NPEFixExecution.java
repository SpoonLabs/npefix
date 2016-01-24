package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.resi.selector.Selector;
import org.json.JSONObject;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NPEFixExecution implements Comparable<NPEFixExecution>{
	private final Selector strategySelector;
	private Set<Location> locations;
	private Map<fr.inria.spirals.npefix.resi.context.Decision, Integer> nbApplication;
	private Map<fr.inria.spirals.npefix.resi.context.Decision, Integer> currentIndex;
	private Result testResult;
	private Method test;
	private List<fr.inria.spirals.npefix.resi.context.Decision> decisions = null;
	private Map<String, Object> metadata = new HashMap();
	private Date startDate;

	public NPEFixExecution(Selector strategySelector) {
		this.locations = new HashSet<>();
		nbApplication = new HashMap<>();
		currentIndex = new HashMap<>();
		decisions = new ArrayList<>();
		startDate = new Date();
		this.strategySelector = strategySelector;
	}

	public void increaseNbApplication(
			fr.inria.spirals.npefix.resi.context.Decision decision) {
		if(!decisions.contains(decision)) {
			decisions.add(decision);
		}
		if(nbApplication.containsKey(decision)) {
			nbApplication.put(decision, nbApplication.get(decision) + 1);
		} else {
			nbApplication.put(decision, 1);
		}
		if(!currentIndex.containsKey(decision)) {
			currentIndex.put(decision, 0);
		}
	}

	public void addApplication(
			fr.inria.spirals.npefix.resi.context.Decision decision) {
		locations.add(decision.getLocation());
		increaseNbApplication(decision);
	}

	public Map<fr.inria.spirals.npefix.resi.context.Decision, Integer> getNbApplication() {
		return nbApplication;
	}

	public Set<Location> getLocations() {
		return locations;
	}

	public Map<fr.inria.spirals.npefix.resi.context.Decision, Integer> getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(Map<fr.inria.spirals.npefix.resi.context.Decision, Integer> currentIndex) {
		//locations.addAll(currentIndex.keySet());
		this.currentIndex = currentIndex;
	}

	public Result getTestResult() {
		return testResult;
	}

	public void setTestResult(Result testResult) {
		this.testResult = testResult;
	}

	public Method getTest() {
		return test;
	}

	public void setTest(Method test) {
		this.test = test;
	}

	public List<fr.inria.spirals.npefix.resi.context.Decision> getDecisions() {
		return decisions;
	}

	public void addDecision(
			fr.inria.spirals.npefix.resi.context.Decision mainDecision) {
		this.decisions.add(mainDecision);
	}

	public Object putMetadata(String key, Object value) {
		return metadata.put(key, value);
	}

	public Object getMetadata(String key) {
		return metadata.get(key);
	}

	public JSONObject toJSON() {
		JSONObject output = new JSONObject();
		// strategy
		for (int i = 0; i < decisions.size(); i++) {
			fr.inria.spirals.npefix.resi.context.Decision decision = decisions.get(i);
			output.append("decisions", decision.toJSON());
		}
		output.put("date", startDate.getTime());

		// test
		JSONObject jsonTest = new JSONObject();
		output.put("test", jsonTest);
		jsonTest.put("class", test.getDeclaringClass().getName());
		jsonTest.put("name", test.getName());

		JSONObject resultJSON = new JSONObject();
		output.put("result", resultJSON);
		resultJSON.put("success", testResult.wasSuccessful());
		resultJSON.put("failureCount", testResult.getFailureCount());
		resultJSON.put("runCount", testResult.getRunCount());
		resultJSON.put("runTime", testResult.getRunTime());
		resultJSON.put("metadata", metadata);
		for (int i = 0; i < testResult.getFailures().size(); i++) {
			Failure failure = testResult.getFailures().get(i);
			JSONObject failureJSON = new JSONObject();
			failureJSON.put("classname", failure.getDescription().getClassName());
			failureJSON.put("methodname", failure.getDescription().getMethodName());
			failureJSON.put("failure", failure);
			failureJSON.put("message", failure.getException().getMessage());
			failureJSON.put("exception", failure.getException());
			jsonTest.append("error", failureJSON);
		}

		for (Iterator<Location> iterator = locations.iterator(); iterator.hasNext(); ) {
			Location location = iterator.next();
			JSONObject locationJSON = new JSONObject();
			locationJSON.put("class", location.className);
			locationJSON.put("line", location.line);
			locationJSON.put("sourceStart", location.sourceStart);
			locationJSON.put("sourceEnd", location.sourceEnd);

			Integer executionCount = nbApplication.get(location);
			if(executionCount == null) {
				executionCount = 0;
			}
			locationJSON.put("executionCount", executionCount);

			/*if(possibleValues.containsKey(location)
					&& possibleValues.get(location).size() > currentIndex.get(location)) {
				JSONObject valueJSON = new JSONObject();
				Object value = possibleValues.get(location)
						.get(currentIndex.get(location));
				valueJSON.put("value", value!=null?value.toString():null);
				valueJSON.put("class", value!=null?value.getClass().getCanonicalName():null);
				locationJSON.accumulate("values", valueJSON);
			} else if(possibleVariables.containsKey(location)
					&& possibleVariables.get(location).size() > currentIndex.get(location)) {
				JSONObject valueJSON = new JSONObject();
				String variable = possibleVariables.get(location).keySet()
						.toArray(new String[]{})[currentIndex.get(location)];
				Object value = possibleVariables.get(location).get(variable);
				valueJSON.put("variable", variable);
				valueJSON.put("value", value!=null?value.toString():null);
				valueJSON.put("class", value!=null?value.getClass().getCanonicalName():null);
				locationJSON.accumulate("values", valueJSON);
			}*/
			output.append("locations", locationJSON);
		}

		return output;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		if(test != null) {
			output.append(test.getDeclaringClass().getCanonicalName() + "#" + test.getName());
		}

		if(decisions.isEmpty()) {
			output.append("<No Strat> ");
		} else {
			for (int i = 0; i < decisions.size(); i++) {
				fr.inria.spirals.npefix.resi.context.Decision decision =  decisions.get(i);
				output.append("\t" + (i + 1) + " " + decision.toString() + "\n");
			}
		}

		output.append("Status: ");
		if(getTestResult()!=null) {
			output.append(getTestResult().wasSuccessful() ? "Ok" : "Ko");
		} else {
			output.append("pending...");
		}
		return output.toString();
	}

	@Override
	public int compareTo(NPEFixExecution o) {
		Decision thisDecision = null;
		if(!decisions.isEmpty()) {
			thisDecision = decisions.get(0);
		}
		Decision oDecision = null;
		if(!o.decisions.isEmpty()) {
			oDecision = o.decisions.get(0);
		}
		return (this.getTest().getDeclaringClass().getCanonicalName() + this.getTest().getName() + thisDecision).compareTo(o.getTest().getDeclaringClass().getCanonicalName() + o.getTest().getName() + oDecision);
	}
}
