package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.patchTemplate.InstanceCreator;
import fr.inria.spirals.npefix.patchTemplate.ThisFinder;
import fr.inria.spirals.npefix.patchTemplate.VariableFinder;
import fr.inria.spirals.npefix.patchTemplate.template.PatchTemplate;
import fr.inria.spirals.npefix.patchTemplate.template.ReplaceGlobal;
import fr.inria.spirals.npefix.patchTemplate.template.ReplaceLocal;
import fr.inria.spirals.npefix.patchTemplate.template.SkipLine;
import fr.inria.spirals.npefix.patchTemplate.template.SkipMethodReturn;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.context.instance.Instance;
import fr.inria.spirals.npefix.resi.context.instance.InstanceFactory;
import fr.inria.spirals.npefix.resi.context.instance.NewInstance;
import fr.inria.spirals.npefix.resi.context.instance.PrimitiveInstance;
import fr.inria.spirals.npefix.resi.context.instance.VariableInstance;
import fr.inria.spirals.npefix.resi.oracle.TestOracle;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.runner.Request;
import org.junit.runner.Result;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.EarlyTerminatingScanner;
import utils.TestClassesFinder;
import utils.sacha.runner.main.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NPEFixTemplateEvaluation extends AbstractNPEDataset {

    private Map<String, NpePosition> positions = new HashMap<>();

    public NPEFixTemplateEvaluation() {
        positions.put(COLLECTIONS_360, new NpePosition("org.apache.commons.collections.iterators.FilterListIterator", 6901, 6908));
        positions.put(FELIX4960, new NpePosition("org.apache.felix.framework.BundleRevisionImpl", 17256, 17266));
        positions.put(LANG_304, new NpePosition("org.apache.commons.lang.LocaleUtils", 8735, 8753));
        positions.put(LANG_587, new NpePosition("org.apache.commons.lang3.ClassUtils", 37644, 37651));
        positions.put(LANG_703, new NpePosition("org.apache.commons.lang3.StringUtils", 136949, 136976));
        positions.put(MATH_290, new NpePosition("org.apache.commons.math.optimization.linear.SimplexTableau", 10261, 10318));
        positions.put(MATH_305, new NpePosition("org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer", 3741, 3747));
        positions.put(MATH_369, new NpePosition("org.apache.commons.math.analysis.solvers.BisectionSolver", 3127, 3127));
        positions.put(MATH_988_A, new NpePosition("org.apache.commons.math3.geometry.euclidean.twod.Line", 7235, 7236));
        positions.put(MATH_988_B, new NpePosition("org.apache.commons.math3.geometry.euclidean.threed.Line", 4565, 4569));
        positions.put(MATH_1115, new NpePosition("org.apache.commons.math3.geometry.partitioning.BSPTree", 11138, 11138));
        positions.put(MATH_1117, new NpePosition("org.apache.commons.math3.geometry.partitioning.BSPTree", 11138, 11138));
        positions.put(PDFBOX_2812, new NpePosition("org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory", 8817, 8823));
        positions.put(PDFBOX_2965, new NpePosition("org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm", 8014, 8019));
        positions.put(PDFBOX_2995, new NpePosition("org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm", 11526, 11542));
        positions.put(SLING_4982, new NpePosition("org.apache.sling.security.impl.ContentDispositionFilter$RewriterResponse", 12523, 12527));
    }

    @Override
    protected NPEOutput runProject(String name, String source, String test, String[] deps) {
		NPEOutput output = new NPEOutput();

		// build the model
        spoon.Launcher spoon = new spoon.Launcher();
        spoon.addInputResource(new File(source).getAbsolutePath());
		spoon.addInputResource(test);

		// define the classpath
		String[] classpathDep = depArrayToClassPath(deps).split(File.pathSeparator);
		String[] classpath = new String[classpathDep.length + 1];
		classpath[0] = Config.CONFIG.getEvaluationWorkingDirectory() + "/" + name + "/bin";
		System.arraycopy(classpathDep, 0, classpath, 1, classpathDep.length);
		spoon.getModelBuilder().setSourceClasspath(classpath);

        spoon.buildModel();

		Date initEndDate = new Date();

        if (positions.containsKey(name)) {
			CtElement element = getElementFromPosition(spoon.getFactory().Type().get(positions.get(name).getClassname()), positions.get(name));
			if (element != null) {
				output.addAll(strategy1(spoon, (CtExpression) element));
				output.addAll(strategy2(spoon, (CtExpression) element));
				output.addAll(strategy3(spoon, (CtExpression) element));
				output.addAll(strategy4(spoon, (CtExpression) element));
			} else {
				Assert.fail("element not found");
			}
        } else {
            Assert.fail(name + " is not handled");
        }
        output.setEnd(new Date());
		Set<Decision> decisions = new HashSet<>();
		for (int i = 0; i < output.size(); i++) {
			Lapse lapse = output.get(i);
			decisions.addAll(lapse.getDecisions());
		}
		JSONObject jsonObject = output.toJSON(spoon);

		jsonObject.put("endInit", initEndDate.getTime());
		for (Decision decision : decisions) {
			jsonObject.append("searchSpace", decision.toJSON());
		}
		serializeResult(jsonObject, name, "Template");
		printResults(output, false);
        return output;
    }

	private CtTypeReference getExpectedType(CtExpression element) {
		CtTypeReference type = element.getType();
		if (element.getParent() instanceof CtInvocation) {
			type = (((CtInvocation) element.getParent()).getExecutable().getDeclaringType());
		} else if (element.getParent() instanceof CtLocalVariable) {
			type = (((CtLocalVariable) element.getParent()).getType());
		} else if (element.getParent() instanceof CtAssignment) {
			type = (((CtAssignment) element.getParent()).getType());
		} else {
			System.out.println(element.getParent());
		}
		return type;
	}

	private List<CtExpression> getVariableAccesses(CtElement elementToReplace, CtTypeReference typeOfElement) {
		List<CtExpression> output = new ArrayList<>();
		List<CtVariable> ctVariables = new VariableFinder(elementToReplace).find(typeOfElement);
		for (int i = 0; i < ctVariables.size(); i++) {
			CtVariable ctVariable = ctVariables.get(i);
			if (elementToReplace instanceof CtVariableAccess &&
					ctVariable.equals(((CtVariableAccess) elementToReplace).getVariable().getDeclaration())) {
				// ignore the current null element
				continue;
			}
			boolean isStatic = ctVariable.hasModifier(ModifierKind.STATIC);
			CtVariableAccess variableRead = ctVariable.getFactory().Code().createVariableRead(ctVariable.getReference(), isStatic);
			if (variableRead instanceof CtFieldAccess) {
				((CtFieldAccess) variableRead).setTarget(null);
			}
			if (ctVariable.getType() instanceof CtArrayTypeReference && !VariableFinder.isAssignableFrom(typeOfElement, ctVariable.getType())) {
				CtArrayRead arrayRead = ctVariable.getFactory().Core().createArrayRead();
				arrayRead.setTarget(variableRead);
				arrayRead.setIndexExpression(ctVariable.getFactory().Code().createLiteral(0));
				output.add(arrayRead);
			} else {
				output.add(variableRead);
			}
		}
		output.addAll(new ThisFinder(elementToReplace).find(typeOfElement));
		return output;
	}

	/**
	 * Replace null element by existing element
	 */
	private NPEOutput strategy1(Launcher launcher, CtExpression element) {
		NPEOutput output = new NPEOutput();

		Location location = getLocation(element);

		CtTypeReference type = getExpectedType(element);

		List<CtExpression> variables = getVariableAccesses(element, type);
		for (int i = 0; i < variables.size(); i++) {
			CtExpression ctVariableAccess = variables.get(i);
			Decision decision = new Decision<>(new Strat1A(),
					location,
					InstanceFactory.fromCtExpression(ctVariableAccess));
			output.addAll(applyAndRunPatch(launcher, new ReplaceLocal(ctVariableAccess), element, decision));
		}
		// in global mode the new element as to be the same type
		variables = getVariableAccesses(element, element.getType());
		for (int i = 0; i < variables.size(); i++) {
			CtExpression ctVariableAccess = variables.get(i);
			Decision decision = new Decision<>(new Strat1B(),
					location,
					InstanceFactory.fromCtExpression(ctVariableAccess));
			output.addAll(applyAndRunPatch(launcher, new ReplaceGlobal(ctVariableAccess), element,
					decision));
		}
		return output;
	}

	/**
	 * Replace null element by new instance
	 */
	private NPEOutput strategy2(Launcher launcher, CtExpression element) {
		NPEOutput output = new NPEOutput();

		Location location = getLocation(element);

		CtTypeReference type = getExpectedType(element);

		List<CtExpression> instances = new InstanceCreator(type).create();
		for (int i = 0; i < instances.size(); i++) {
			CtExpression newInstance = instances.get(i);

			Decision decision = new Decision<>(new Strat2A(), location,
					InstanceFactory.fromCtExpression(newInstance));
			output.addAll(applyAndRunPatch(launcher, new ReplaceLocal(newInstance), element,
					decision));
		}

		// in global mode the new element as to be the same type
		instances = new InstanceCreator(element.getType()).create();
		for (int i = 0; i < instances.size(); i++) {
			CtExpression newInstance = instances.get(i);

			Decision decision = new Decision<>(new Strat2B(),
					location,
					InstanceFactory.fromCtExpression(newInstance));
			output.addAll(applyAndRunPatch(launcher, new ReplaceGlobal(newInstance), element,
					decision));
		}
		return output;
	}

	/**
	 * Add check not null around null element
	 */
	private NPEOutput strategy3(Launcher launcher, CtExpression element) {
		NPEOutput output = new NPEOutput();

		Location location = getLocation(element);

		Decision decision = new Decision<>(new Strat3(), location, new PrimitiveInstance(false));

		output.addAll(applyAndRunPatch(launcher, new SkipLine(), element,
				decision));
		return output;
	}

	private Location getLocation(CtExpression element) {
		return new Location(element.getParent(CtClass.class).getQualifiedName(),
					element.getPosition().getLine(),
					element.getPosition().getSourceStart(),
					element.getPosition().getSourceEnd());
	}

	/**
	 * Skip method
	 */
	private NPEOutput strategy4(Launcher launcher, CtExpression element) {
		NPEOutput output = new NPEOutput();
		CtMethod method = element.getParent(CtMethod.class);

		Location location = getLocation(element);

		if (method != null) {
			CtTypeReference expectedType = method.getType();

			List<CtExpression> expressions = new ArrayList<>();

			expressions.addAll(getVariableAccesses(element, expectedType));
			expressions.addAll(new InstanceCreator(expectedType).create());
			// return null or void
			expressions.add(null);

			for (int i = 0; i < expressions.size(); i++) {
				CtExpression ctVariable = expressions.get(i);

				Instance instance = InstanceFactory.fromCtExpression(ctVariable);
				ReturnType type;
				if (ctVariable == null) {
					type = ReturnType.NULL;
				} else if (instance instanceof VariableInstance) {
					type = ReturnType.VAR;
				} else  if (instance instanceof NewInstance) {
					type = ReturnType.NEW;
				} else {
					type = ReturnType.NEW;
				}

				Decision decision = new Decision<>(new Strat4(type), location, instance);
				output.addAll(applyAndRunPatch(launcher, new SkipMethodReturn(ctVariable), element, decision));
			}
		}
		return output;
	}

	private List<Lapse> applyAndRunPatch(Launcher launcher,
			PatchTemplate patchTemplate,
			CtExpression expression, Decision decision) {
		// clone the parent of the expression
		CtType originalType = expression.getParent(CtType.class);
		CtType cloneType = originalType.clone();
		CtExpression clonedElement = (CtExpression) getElementFromPosition(cloneType,
				new NpePosition(cloneType.getQualifiedName(),
						expression.getPosition().getSourceStart(),
						expression.getPosition().getSourceEnd()));

		try {
			System.out.println(patchTemplate.apply(clonedElement));

			// change the model with the new class
			originalType.replace(cloneType);

			List<Lapse> lapses = executeTest(launcher, decision);

			// restore the original class
			cloneType.replace(originalType);

			return lapses;
		} catch (RuntimeException e) {
			// the patch template cannot by applied on expression
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private List<Lapse> executeTest(final Launcher launcher, final Decision decision) {
		Path tempBinFolder = null;
		try {
			tempBinFolder = Files.createTempDirectory("npefix_");

			String sourceOutput = tempBinFolder + File.separator + "source";
			File binFolder = new File(tempBinFolder + File.separator + "bin");

			launcher.getEnvironment().setComplianceLevel(7);
			launcher.getEnvironment().setCopyResources(true);
			launcher.setSourceOutputDirectory(sourceOutput);
			launcher.getModelBuilder().setBinaryOutputDirectory(binFolder);
			launcher.prettyprint();
			launcher.getModelBuilder().compile();

			copyResources(sourceOutput, binFolder.getAbsolutePath());

			final String[] sourceClasspath = launcher.getModelBuilder().getSourceClasspath();
			URL[] classloader = new URL[sourceClasspath.length];

			classloader[0] = binFolder.toURL();
			for (int i = 1; i < sourceClasspath.length; i++) {
				String s = sourceClasspath[i];
				classloader[i] = new File(s).toURL();
			}
			URLClassLoader testClassLoader = new URLClassLoader(classloader,
					Thread.currentThread().getContextClassLoader());
			final String[] testsString = new TestClassesFinder().findIn(testClassLoader, false);

			NPEOutput lapses = new NPEOutput();

			final TestRunner testRunner = new TestRunner();
			for (int i = 0; i < testsString.length; i++) {
				String testClassName = testsString[i];
				testClassLoader = new URLClassLoader(classloader, Thread.currentThread().getContextClassLoader());
				List<String> methodTests = fr.inria.spirals.npefix.main.all.Launcher.getTests(launcher, testClassLoader);

				for (int j = 0; j < methodTests.size(); j++) {
					String method = methodTests.get(j);
					String[] split = method.split("#");
					method = split[1];

					final Request request;
					try {
						request = Request.method(testClassLoader.loadClass(testClassName), method);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						continue;
					}

					Lapse lapse = new Lapse(null);
					lapse.setTestClassName(testClassName);
					lapse.setTestName(method);
					lapse.addDecision(decision);

					decision.setUsed(true);

					Result result = testRunner.run(request);

					lapse.setOracle(new TestOracle(result));
					lapse.setEndDate(new Date());
					lapse.setFinished(true);

					System.out.println(lapse);

					lapses.add(lapse);
				}
			}

			return lapses;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tempBinFolder != null) {
				deleteFile(tempBinFolder.toFile());
			}
		}
		return Collections.emptyList();
	}

	@Override
    public void eval(NPEOutput results) {
    }

    private CtElement getElementFromPosition(CtType type, final NpePosition position) {
        EarlyTerminatingScanner<CtElement> scanner = new EarlyTerminatingScanner<CtElement>() {
            @Override
            protected void enter(CtElement e) {
                if (e.getPosition() != null
                        && e.getPosition().getSourceStart() == position.getStart()
                        && e.getPosition().getSourceEnd() == position.getEnd()) {
                    setResult(e);
                    terminate();
                }
                super.enter(e);
            }
        };
        scanner.scan(type);
        return scanner.getResult();
    }

    private class NpePosition {
        private String classname;
        private int start;
        private int end;

        public NpePosition(String classname, int start, int end) {
            this.classname = classname;
            this.start = start;
            this.end = end;
        }

        public String getClassname() {
            return classname;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }

	public static void deleteFile(File element) {
		if (element.isDirectory()) {
			for (File sub : element.listFiles()) {
				deleteFile(sub);
			}
		}
		element.delete();
	}

	private void copyResources(String sourceOutput, String binOutput) {
		File directory = new File(sourceOutput);
		if(!directory.exists()) {
			directory.mkdirs();
		}
		Collection resources = FileUtils
				.listFiles(directory, Launcher.RESOURCES_FILE_FILTER, Launcher.ALL_DIR_FILTER);
		Iterator var6 = resources.iterator();

		while(var6.hasNext()) {
			Object resource = var6.next();
			String resourceParentPath = ((File)resource).getParent();
			String packageDir = resourceParentPath.substring(directory.getPath().length());
			packageDir = packageDir.replace("/java", "").replace("/resources", "");
			String targetDirectory = binOutput + packageDir;

			try {
				FileUtils.copyFileToDirectory((File) resource, new File(targetDirectory));
			} catch (IOException var12) {
				throw new SpoonException(var12);
			}
		}
	}
}
