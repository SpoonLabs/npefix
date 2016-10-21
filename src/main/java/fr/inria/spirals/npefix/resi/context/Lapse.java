package fr.inria.spirals.npefix.resi.context;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.patch.generator.PatchesGenerator;
import fr.inria.spirals.npefix.resi.oracle.Oracle;
import fr.inria.spirals.npefix.resi.selector.Selector;
import org.json.JSONObject;
import spoon.Launcher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Lapse implements Comparable<Lapse>, Serializable {

	private static final long serialVersionUID = 1L;

	private static int currentUniqueId = 0;

	private final Selector strategySelector;
	private final int uniqueId;
	private Set<Location> locations;
	private Map<Decision, Integer> nbApplication;
	private Map<Decision, Integer> currentIndex;
	private Oracle oracle;
	private String testClassName;
	private String testName;
	private List<Decision> decisions = null;
	private Map<String, Object> metadata = new HashMap<>();
	private Date startDate;
	private Date endDate;
	private boolean isFinished = false;

	public Lapse(Selector strategySelector) {
		this.uniqueId = currentUniqueId++;
		this.locations = new HashSet<>();
		nbApplication = new HashMap<>();
		currentIndex = new HashMap<>();
		decisions = new ArrayList<>();
		startDate = new Date();
		this.strategySelector = strategySelector;
		metadata.put("seed", Config.CONFIG.getRandomSeed());
	}

	public void increaseNbApplication(Decision decision) {
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

	public void addApplication(Decision decision) {
		locations.add(decision.getLocation());
		increaseNbApplication(decision);
	}

	public Map<Decision, Integer> getNbApplication() {
		return nbApplication;
	}

	public Set<Location> getLocations() {
		return locations;
	}

	public Map<Decision, Integer> getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(Map<Decision, Integer> currentIndex) {
		//locations.addAll(currentIndex.keySet());
		this.currentIndex = currentIndex;
	}

	public Oracle getOracle() {
		return oracle;
	}

	public void setOracle(Oracle oracle) {
		this.oracle = oracle;
	}

	public String getTestClassName() {
		return testClassName;
	}

	public void setTestClassName(String testClassName) {
		this.testClassName = testClassName;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public List<Decision> getDecisions() {
		return decisions;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void addDecision(Decision mainDecision) {
		this.decisions.add(mainDecision);
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean finished) {
		isFinished = finished;
	}

	public Object putMetadata(String key, Object value) {
		return metadata.put(key, value);
	}

	public Object getMetadata(String key) {
		return metadata.get(key);
	}

	public String toDiff(Launcher spoon) {
		try {
			PatchesGenerator patchesGenerator = new PatchesGenerator(decisions, spoon);
			return patchesGenerator.getDiff();
		} catch (Exception e) {
			return null;
		}
	}

	public JSONObject toJSON(Launcher spoon) {
		JSONObject output = new JSONObject();
		// strategy
		for (int i = 0; i < decisions.size(); i++) {
			Decision decision = decisions.get(i);
			output.append("decisions", decision.toJSON());
		}

		if (spoon != null) {
			output.put("diff", toDiff(spoon));
		}

		output.put("startDate", startDate.getTime());
		output.put("endDate", endDate.getTime());

		// test
		JSONObject jsonTest = new JSONObject();
		output.put("test", jsonTest);
		jsonTest.put("class", testClassName);
		jsonTest.put("name", testName);
		output.put("metadata", metadata);
		output.put("result", oracle.toJSON());

		for (Location location : locations) {
			JSONObject locationJSON = location.toJSON();

			Integer executionCount = nbApplication.get(location);
			if (executionCount == null) {
				executionCount = 0;
			}
			locationJSON.put("executionCount", executionCount);
			output.append("locations", locationJSON);
		}

		return output;
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		if(testClassName != null) {
			output.append(testClassName + "#" + testName + "\n");
		}

		if(decisions.isEmpty()) {
			output.append("<No Strat> ");
		} else {
			for (int i = 0; i < decisions.size(); i++) {
				Decision decision =  decisions.get(i);
				output.append("\t" + (i + 1) + " " + decision.toString() + "\n");
			}
		}

		output.append("Status: ");
		if(getOracle()!=null) {
			output.append(getOracle().isValid() ? "Ok" : "Ko");
		} else {
			output.append("pending...");
		}
		return output.toString();
	}

	@Override
	public int compareTo(Lapse o) {
		Decision thisDecision = null;
		if(!decisions.isEmpty()) {
			thisDecision = decisions.get(0);
		}
		Decision oDecision = null;
		if(!o.decisions.isEmpty()) {
			oDecision = o.decisions.get(0);
		}
		return (testClassName + testName + thisDecision).compareTo(o.testClassName + o.testName + oDecision);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Lapse lapse = (Lapse) o;

		return uniqueId == lapse.uniqueId;

	}

	@Override
	public int hashCode() {
		return uniqueId;
	}
}
