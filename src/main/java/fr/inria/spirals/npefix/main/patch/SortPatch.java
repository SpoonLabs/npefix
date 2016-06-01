package fr.inria.spirals.npefix.main.patch;

import fr.inria.spirals.npefix.patch.PositionScanner;
import fr.inria.spirals.npefix.resi.NPEDataset;
import fr.inria.spirals.npefix.resi.context.Location;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;

public class SortPatch {

	/**
	 *
	 * @param args
	 * 		args[0] project name
	 * 		args[1] path to original source of the project
	 * 		args[2] path to json output
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String projectName = getProjectName(args[0]);
		spoon.Launcher spoon = new spoon.Launcher();
		spoon.getModelBuilder().setSourceClasspath(getClasspath(projectName).split(":"));
		spoon.addInputResource(args[1]);
		spoon.buildModel();

		File file = new File(args[2]);
		JSONTokener tokener = new JSONTokener(new FileReader(file));
		JSONObject root = new JSONObject(tokener);
		JSONArray executions = root.getJSONArray("executions");
		for (Object execution : executions) {
			if (!((JSONObject) execution).getJSONObject("result").getBoolean("success")) {
				continue;
			}

			int score = 0;

			JSONArray decisions = ((JSONObject) execution).getJSONArray("decisions");
			for (Object decision : decisions) {
				System.out.println(decision);
				JSONObject location = ((JSONObject) decision).getJSONObject("location");

				String aClass = location.getString("class");
				int line = location.getInt("line");
				int start = location.getInt("sourceStart");
				int end = location.getInt("sourceEnd");

				CtType objectCtType = spoon.getFactory().Type().get(aClass);
				CtElement element = null;

				PositionScanner positionScanner = new PositionScanner(new Location(aClass, line, start, end));
				try {
					objectCtType.accept(positionScanner);
					throw new RuntimeException("Element not found");
				} catch (RuntimeException e) {
					element = positionScanner.getResult();
				}

				System.out.println(element.getParent());
			}
		}
	}

	private static String getProjectName(String name) {
		return name.replace(" ", "").replace("-", "").toLowerCase();
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
