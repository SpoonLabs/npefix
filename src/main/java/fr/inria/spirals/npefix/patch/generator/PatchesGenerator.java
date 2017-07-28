package fr.inria.spirals.npefix.patch.generator;

import com.cloudbees.diff.Diff;
import fr.inria.spirals.npefix.patch.DecisionElement;
import fr.inria.spirals.npefix.patch.PositionScanner;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import org.apache.commons.io.FileUtils;
import spoon.Launcher;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatchesGenerator {
	private List<Decision> decisions;
	private Launcher spoon;

	public PatchesGenerator(List<Decision> decisions, Launcher spoon) {
		this.decisions = new ArrayList<>();
		for (int i = 0; i < decisions.size(); i++) {
			Decision decision = decisions.get(i);
			if (decision.isUsed()) {
				this.decisions.add(decision);
			}
		}
		this.spoon = spoon;
	}

	public String getDiff() {
		StringBuilder output = new StringBuilder();
		if (decisions.isEmpty()) {
			return output.toString();
		}

		// group decision per file
		Map<String, List<Decision>> classes = new HashMap<>(decisions.size());
		for (int i = 0; i < decisions.size(); i++) {
			Decision decision = decisions.get(i);
			String className = decision.getLocation().getClassName();
			if (!classes.containsKey(className)) {
				classes.put(className, new ArrayList<Decision>(1));
			}
			classes.get(className).add(decision);
		}
		// generate the patch for each file
		Set<String> classNames = classes.keySet();
		for (Iterator<String> iterator = classNames.iterator(); iterator.hasNext(); ) {
			String className = iterator.next();
			List<Decision> decisions = classes.get(className);

			String patch = createPatchDiff(className, decisions);

			output.append(patch);
		}

		// group each patch
		return output.toString();
	}

	private String createPatchDiff(String className, List<Decision> decisions) {
		CtType type = getCtType(className);

		// get the content of the class
		String originalClassContent = getFileContent(type.getPosition().getFile());
		String classContent = originalClassContent;

		List<DecisionElement> elements = getElements(type, decisions);
		Map<Integer, List<DecisionElement>> elementsPerLine = new HashMap<>(decisions.size());
		for (int i = 0; i < elements.size(); i++) {
			DecisionElement decisionElement = elements.get(i);
			CtElement element = decisionElement.getElement();
			if (element == null) {
				continue;
			}
			SourcePosition position = element.getPosition();
			if (position != null) {
				int line = position.getLine();
				if (!elementsPerLine.containsKey(line)) {
					elementsPerLine.put(line, new ArrayList<DecisionElement>());
				}
				elementsPerLine.get(line).add(decisionElement);
			}
		}

		int[] offset = new int[classContent.split("\n").length];
		int[] offsetLine = new int[offset.length];
		List<Integer> lines = new ArrayList<>(elementsPerLine.keySet());
		Collections.sort(lines);
		for (int j = 0; j < lines.size(); j++) {
			Integer line =  lines.get(j);
			List<DecisionElement> decisionElements = elementsPerLine.get(line);

			if (decisionElements.size() > 1) {
				boolean isSameDecision = true;
				String strategyName = null;
				String element = null;
				for (int i = 0; i < decisionElements.size(); i++) {
					DecisionElement decisionElement = decisionElements.get(i);
					decisionElement.setClassContent(classContent);

					String currentStrategy = decisionElement.getDecision().getStrategy().getName();
					if (strategyName == null) {
						strategyName = currentStrategy;
						element = decisionElement.getElement().toString();
					} else {
						// if the strategy is different or the element is different
						if (!strategyName.equals(currentStrategy)
								|| !element.equals(decisionElement.getElement().toString())) {
							isSameDecision = false;
							break;
						}
					}
				}
				if (true  || isSameDecision) {
					PatchGenerator patchGenerator = new PatchGenerator(decisionElements, spoon, offset, offsetLine);
					classContent = patchGenerator.getPatch();

					offset = patchGenerator.getOffset();
					offsetLine = patchGenerator.getOffsetLine();
				} else {
					System.err.println("Big problem need ternary");
				}
			} else {
				DecisionElement decisionElement = decisionElements.get(0);
				decisionElement.setClassContent(classContent);
				PatchGenerator patchGenerator = new PatchGenerator(decisionElements, spoon, offset, offsetLine);
				classContent = patchGenerator.getPatch();

				offset = patchGenerator.getOffset();
				offsetLine = patchGenerator.getOffsetLine();
			}
		}

		StringReader r1 = new StringReader(originalClassContent);
		StringReader r2 = new StringReader(classContent);
		String diff = null;
		try {
			String path = getClassPath(type);
			diff = Diff.diff(r1, r2, false)
					.toUnifiedDiff(path,
							path,
							new StringReader(originalClassContent),
							new StringReader(classContent), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return diff.replaceAll("\n\\\\ No newline at end of file", "");
	}

	private String getClassPath(CtType type) {
		String path = type.getPosition().getFile().getPath();
		String intersection = null;
		Set<File> inputSources = spoon.getModelBuilder().getInputSources();
		for (File inputSource : inputSources) {
			if (intersection == null) {
				intersection = inputSource.getPath();
			} else {
				intersection = intersection(intersection, inputSource.getPath());
			}
		}
		path = path.replace(intersection, "");
		return path;
	}

	/**
	 * Get the intersection of two paths
 	 * @param s1
	 * @param s2
	 * @return
	 */
	private String intersection(String s1, String s2) {
		String[] split1 = s1.split("/");
		String[] split2 = s2.split("/");

		StringBuilder output = new StringBuilder();

		for (int i = 0; i < split1.length && i < split2.length; i++) {
			String path1 = split1[i];
			String path2 = split2[i];
			if (path1.equals(path2)) {
				output.append(path1);
				output.append("/");
			} else {
				break;
			}
		}
		return output.toString();
	}

	/**
	 * Get the content of the file
	 * @param file
	 * @return
	 */
	private String getFileContent(File file) {
		try {
			return FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new RuntimeException("File not found");
		}
	}

	/**
	 * Get the CtType from the full qualified name of a class
	 * @param className
	 * @return
	 */
	private CtType getCtType(String className) {
		CtType type = spoon.getFactory().Type().get(className);
		if (type == null) {
			throw new RuntimeException(className + " type not found.");
		}
		return type;
	}

	private List<DecisionElement> getElements(CtType type, List<Decision> decisions) {
		List<DecisionElement> elements = new ArrayList<>();

		for (int i = 0; i < decisions.size(); i++) {
			Decision decision = decisions.get(i);

			DecisionElement element = getElement(type, decision);

			elements.add(element);
		}
		return elements;
	}

	private DecisionElement getElement(CtType type, Decision decision) {
		final Location location = decision.getLocation();
		PositionScanner positionScanner = new PositionScanner(location);
		try {
			type.accept(positionScanner);
		} catch (RuntimeException e) {
			return new DecisionElement(positionScanner.getResult(), decision);
		}
		throw new RuntimeException("Element not found: " + location);
	}
}
