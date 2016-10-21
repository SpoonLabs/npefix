package fr.inria.spirals.npefix.main.patch;

import fr.inria.spirals.npefix.patch.generator.PatchesGenerator;
import fr.inria.spirals.npefix.patch.sorter.Experiment;
import fr.inria.spirals.npefix.patch.sorter.tokenizer.FullTokenizer;
import fr.inria.spirals.npefix.resi.AbstractNPEDataset;
import fr.inria.spirals.npefix.resi.NPEDataset;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.instance.AbstractInstance;
import fr.inria.spirals.npefix.resi.context.instance.NewArrayInstance;
import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.context.instance.NewInstance;
import fr.inria.spirals.npefix.resi.context.instance.PrimitiveInstance;
import fr.inria.spirals.npefix.resi.context.instance.StaticVariableInstance;
import fr.inria.spirals.npefix.resi.context.instance.VariableInstance;
import fr.inria.spirals.npefix.resi.selector.AbstractSelectorEvaluation;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SortPatch {

	/**
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String resultsRootPath = "/home/thomas/git/bandit-repair-experiments/results/2016-May/exhaustive_exploration";
		resultsRootPath = "output/Template";
		String resultsPath = resultsRootPath + "/results.json";
		File file = new File(resultsPath);
		JSONTokener tokener = new JSONTokener(new FileReader(file));
		JSONObject root = new JSONObject(tokener);
		Iterator<String> keys = root.keys();
		while (keys.hasNext()) {
			String project = keys.next();
			if (!project.contains(AbstractNPEDataset.MATH_290)) {
				//continue;
			}
			System.out.println(project);
			long version = root.getLong(project);
			String projectResultPath = resultsRootPath + "/" + project + "/" + version + ".json";
			String projectSourcePath = AbstractSelectorEvaluation.getSourcePathProject(getProjectName(project));
			sortPatchProject(project, projectSourcePath, projectResultPath);
		}
	}

	private static void sortPatchProject(String project, String projectSourcePath, String projectResultPath) throws FileNotFoundException {
		String projectName = getProjectName(project);

		spoon.Launcher spoon = new spoon.Launcher();
		spoon.getModelBuilder().setSourceClasspath(AbstractSelectorEvaluation.getClasspathProject(projectName).split(":"));
		spoon.addInputResource(projectSourcePath);
		spoon.buildModel();

		Experiment experiment = new Experiment();

		File file = new File(projectResultPath);
		JSONTokener tokener = new JSONTokener(new FileReader(file));
		JSONObject root = new JSONObject(tokener);
		JSONArray executions = root.getJSONArray("executions");
		for (Object execution : executions) {
			if (!((JSONObject) execution).getJSONObject("result").getBoolean("success")) {
				//continue;
			}
			List<Decision> decisions = new ArrayList<>();
			if (!((JSONObject) execution).has("decisions")) {
				continue;
			}
			JSONArray jsonDecisions = ((JSONObject) execution).getJSONArray("decisions");
			if (jsonDecisions.length() == 0) {
				continue;
			}
			for (Object jsonDecision : jsonDecisions) {
				JSONObject jsonLocation = ((JSONObject) jsonDecision).getJSONObject("location");

				String aClass = jsonLocation.getString("class");
				int line = jsonLocation.getInt("line");
				int start = jsonLocation.getInt("sourceStart");
				int end = jsonLocation.getInt("sourceEnd");
				Location location = new Location(aClass, line, start, end);
				Strategy strategy = createStrategyFromString(((JSONObject) jsonDecision).getString("strategy"));
				Decision decision = new Decision(strategy, location);
				decision.setDecisionType(Decision.DecisionType.valueOf(((JSONObject) jsonDecision).getString("decisionType")));
				decision.setValue(createInstanceFromJson(((JSONObject) jsonDecision).getJSONObject("value")));
				decision.setUsed(((JSONObject) jsonDecision).getBoolean("used"));
				decisions.add(decision);
			}

			PatchesGenerator patchesGenerator = new PatchesGenerator(decisions, spoon);
			String diff = patchesGenerator.getDiff();
			System.out.println(diff);
			((JSONObject) execution).put("diff", diff);
			String patch = getChange(diff);
			try {
				double score = experiment.probabilityPatch(new File(projectSourcePath), 3, new FullTokenizer(), patch);
				System.out.println(score);
				if (Double.isInfinite(score)) {
					((JSONObject) execution).put("score", Double.MAX_VALUE);
				} else {
					((JSONObject) execution).put("score", score);
				}
			} catch (Exception e) {
				diff = patchesGenerator.getDiff();
				System.out.println(diff);
				((JSONObject) execution).put("diff", diff);
				patch = getChange(diff);
				e.printStackTrace();
				System.out.println(patch);
			}
		}
		try {
			FileWriter fileWriter = new FileWriter(projectResultPath);
			root.write(fileWriter);
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getChange(String diff) {
		StringBuilder output = new StringBuilder();


		String currentPatch = "";
		String[] lines = diff.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (!line.startsWith("+ ")) {
				if (currentPatch != null && !currentPatch.isEmpty()) {
					output.append(currentPatch);
					currentPatch = "";
				}
			} else {
				currentPatch += line.replace("+ ", "") + "\n";
			}
		}

		return output.toString();
	}

	private static Instance createPrimitiveInstanceFromJson(JSONObject object) {
		Object value = object.get("value");
		String clazz = object.getString("class");
		if (value instanceof String && "null".equals(clazz)) {
			return new PrimitiveInstance(null);
		}
		return new PrimitiveInstance<>(value);
	}
	private static Instance createArrayInstanceFromJson(JSONObject object) {
		List<Instance<?>> values = new ArrayList<>();
		if (object.has("values")) {
			JSONArray jsonValues = object.getJSONArray("values");
			for (int i = 0; i < jsonValues.length(); i++) {
				JSONObject o = (JSONObject) jsonValues.get(i);
				values.add(createInstanceFromJson(o));
			}
		}
		return new NewArrayInstance(object.getString("class"), values);
	}
	private static Instance createNewInstanceFromJson(JSONObject object) {
		List<Instance<?>> parameters = new ArrayList<>();
		if (object.has("parameters")) {
			JSONArray jsonValues = object.getJSONArray("parameters");
			for (int i = 0; i < jsonValues.length(); i++) {
				JSONObject o = (JSONObject) jsonValues.get(i);
				parameters.add(createInstanceFromJson(o));
			}
		}
		String[] parameterTypes = new String[0];
		if (object.has("parameters")) {
			JSONArray jsonParameterTypes = object.getJSONArray("parameterTypes");
			parameterTypes = new String[jsonParameterTypes.length()];
			for (int i = 0; i < jsonParameterTypes.length(); i++) {
				String type = jsonParameterTypes.getString(i);
				parameterTypes[i] = type;
			}
		}
		return new NewInstance(object.getString("class"), parameterTypes, parameters);
	}
	private static Instance createVariableInstanceFromJson(JSONObject object) {
		return new VariableInstance(object.getString("variableName"));
	}
	private static Instance createStaticVariableInstanceFromJson(JSONObject object) {
		return new StaticVariableInstance(object.getString("class"), object.getString("fieldName"));
	}
	private static Instance createInstanceFromJson(JSONObject object) {
		if (object.has("instanceType")) {
			switch (object.getString("instanceType")) {
			case "Primitive":
				return createPrimitiveInstanceFromJson(object);
			case "NewArray":
				return createArrayInstanceFromJson(object);
			case "New":
				return createNewInstanceFromJson(object);
			case "StaticVariable":
				return createStaticVariableInstanceFromJson(object);
			case "Variable":
				return createVariableInstanceFromJson(object);
			}
			throw new RuntimeException("Unknown instanceType:" + object.getString("instanceType"));
		} else {
			AbstractInstance value = new VariableInstance(
					object.getString("value"));
			try {
				Class type = value.getClassFromString(object.getString("type"));
				if (type.isPrimitive()) {
					String stringValue = object.getString("value");
					Object primitiveValue = null;
					if (type == int.class) {
						primitiveValue = Integer.getInteger(stringValue);
					} else if (type == int[].class) {
						primitiveValue = Integer.valueOf(stringValue);
					} else if (type == long.class) {
						primitiveValue = Long.valueOf(stringValue);
					} else if (type == long[].class) {
						primitiveValue = Integer.valueOf(stringValue);
					} else if (type == float.class) {
						primitiveValue = Float.valueOf(stringValue);
					} else if (type == float[].class) {
						primitiveValue = Float.valueOf(stringValue);
					} else if (type == double.class) {
						primitiveValue = Double.valueOf(stringValue);
					} else if (type == double[].class) {
						primitiveValue = Double.valueOf(stringValue);
					} else if (type == byte.class) {
						primitiveValue = Byte.valueOf(stringValue);
					} else if (type == byte[].class) {
						primitiveValue = Byte.valueOf(stringValue);
					} else if (type == char.class) {
						primitiveValue = stringValue.charAt(0);
					} else if (type == char[].class) {
						primitiveValue = stringValue.charAt(0);
					} else if (type == boolean.class) {
						primitiveValue = Boolean.valueOf(stringValue);
					} else if (type == boolean[].class) {
						primitiveValue = Boolean.valueOf(stringValue);
					}
					value = new PrimitiveInstance<>(primitiveValue);
				}
			} catch (RuntimeException e) {

			}
			return value;
		}
	}

	private static Strategy createStrategyFromString(String str) {
		switch (str) {
		case "Strat1A":
			return new Strat1A();
		case "Strat1B":
			return new Strat1B();
		case "Strat2A":
			return new Strat2A();
		case "Strat2B":
			return new Strat2B();
		case "Strat3":
			return new Strat3();
		case "Strat4 VAR":
			return new Strat4(ReturnType.VAR);
		case "Strat4 VOID":
			return new Strat4(ReturnType.VOID);
		case "Strat4 NEW":
			return new Strat4(ReturnType.NEW);
		case "Strat4 NULL":
			return new Strat4(ReturnType.NULL);
		}
		return null;
	}

	private static String getProjectName(String name) {
		return name.toLowerCase();
	}

	private static String getClasspath(String name) {
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		Class<NPEDataset> npeDatasetClass = NPEDataset.class;
		try {
			Field field = npeDatasetClass.getField("classpath" + name);
			Object o = field.get(null);
			return (String) o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
