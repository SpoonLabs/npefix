package fr.inria.spirals.npefix.main.run;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import fr.inria.spirals.npefix.main.DecisionServer;
import fr.inria.spirals.npefix.main.all.DefaultRepairStrategy;
import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.main.all.RepairStrategy;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.exception.NoMoreDecision;
import fr.inria.spirals.npefix.resi.selector.ExplorerSelector;
import fr.inria.spirals.npefix.resi.selector.Selector;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class Main {

	private JSAP jsap = new JSAP();

	private List<String> sources = new ArrayList<>();
	private String workingDirectory = ".";
	private String classpath = "";
	private int complianceLevel = 7;
	private RepairStrategy repairStrategy;
	private String[] tests;
	private int nbIteration;
	private String repairStrategyClassname = "fr.inria.spirals.npefix.main.all.DefaultRepairStrategy";

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		try {
			main.initJSAP();
			if (!main.parseArguments(args)) {
				return;
			}
			main.run();
		} catch (Exception e) {
			main.showUsage();
			throw e;
		} finally {

		}
		//System.exit(0);// don't do that, it does not run in a test
	}

	private RepairStrategy getRepairStrategy() throws Exception {
		Class<RepairStrategy> aClass = (Class<RepairStrategy>) this.getClass().getClassLoader().loadClass(this.repairStrategyClassname);
		Constructor<RepairStrategy> constructor = aClass.getConstructor(String[].class);
		return constructor.newInstance(new Object[]{sources.toArray(new String[]{})});
	}

	private NPEOutput run() throws Exception {
		repairStrategy = getRepairStrategy();
		Launcher npefix = new Launcher(sources.toArray(new String[]{}), workingDirectory + "/npefix-src", workingDirectory + "/npefix-bin", classpath, complianceLevel, repairStrategy);
		if (!new File(workingDirectory + "/npefix-bin").exists()) {
			npefix.instrument();
		}

		Date initDate = new Date();

		NPEOutput result = multipleRuns(npefix, Arrays.asList(tests), new ExplorerSelector());

		spoon.Launcher spoon = new spoon.Launcher();
		for (String s : sources) {
			spoon.addInputResource(s);
		}

		spoon.getModelBuilder().setSourceClasspath(classpath.split(":"));
		spoon.buildModel();

		JSONObject jsonObject = result.toJSON(spoon);
		jsonObject.put("endInit", initDate.getTime());
		try {
			for (Decision decision : CallChecker.strategySelector.getSearchSpace()) {
				jsonObject.append("searchSpace", decision.toJSON());
			}
			FileWriter writer = new FileWriter(workingDirectory + "/patches_" + new Date().getTime() + ".json");
			jsonObject.write(writer);
			writer.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private NPEOutput multipleRuns(Launcher  npefix, List<String> npeTests, Selector selector) {
		DecisionServer decisionServer = new DecisionServer(selector);
		decisionServer.startServer();
		List<String> testMethods = npefix.getTests(npeTests.toArray(new String[0]));
		if (testMethods.isEmpty()) {
			throw new RuntimeException("No test found");
		}

		NPEOutput output = new NPEOutput();

		int countError = 0;
		while (output.size() < nbIteration) {
			if(countError > 5) {
				break;
			}
			try {
				List<Lapse> result = npefix.run(selector, testMethods);
				if(result.isEmpty()) {
					countError++;
					continue;
				}
				ArrayList<Lapse> lapses = new ArrayList<>(result);
				for (int i = 0; i < lapses.size(); i++) {
					Lapse lapse = lapses.get(i);
					if (lapse.getDecisions().isEmpty() && lapse.getOracle().isValid()) {
						// passing tests can be ignored
						testMethods.remove(lapse.getTestClassName() + "#" + lapse.getTestName());
						result.remove(lapses);
					}
				}
				boolean isEnd = true;
				for (int i = 0; i < result.size() && isEnd; i++) {
					Lapse lapse = result.get(i);
					if (lapse.getOracle().getError() != null) {
						isEnd = isEnd && lapse.getOracle().getError().contains(NoMoreDecision.class.getSimpleName()) || lapse.getDecisions().isEmpty();
					} else {
						isEnd = false;
					}
				}
				if (isEnd) {
					// no more decision
					countError++;
					continue;
				}
				countError = 0;
				if(output.size() + result.size() > nbIteration) {
					output.addAll(result.subList(0, (nbIteration - output.size())));
				} else {
					output.addAll(result);
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				countError++;
				continue;
			} catch (Exception e) {
				if(e.getCause() instanceof OutOfMemoryError) {
					countError++;
					continue;
				}
				e.printStackTrace();
				countError++;
				continue;
			}
			System.out.println("Multirun " + output.size() + "/" + nbIteration + " " + ((int)(output.size()/(double)nbIteration * 100)) + "%");
		}
		output.setEnd(new Date());
		return output;
	}

	private void showUsage() {
		System.err.println();
		System.err.println("Usage: java -jar npefix.jar");
		System.err.println("                          " + jsap.getUsage());
		System.err.println();
		System.err.println(jsap.getHelp());
	}

	private boolean parseArguments(String[] args) {
		JSAPResult jsapConfig = jsap.parse(args);
		if (!jsapConfig.success()) {
			System.err.println();
			for (Iterator<?> errs = jsapConfig.getErrorMessageIterator(); errs.hasNext(); ) {
				System.err.println("Error: " + errs.next());
			}
			showUsage();
			return false;
		}
		String[] sources = jsapConfig.getStringArray("source");
		for (int i = 0; i < sources.length; i++) {
			String path = sources[i];
			if (new File(path).exists()) {
				this.sources.add(path);
			}
		}
		this.classpath = jsapConfig.getString("classpath");
		this.tests = jsapConfig.getStringArray("test");
		this.nbIteration = jsapConfig.getInt("iteration");
		this.complianceLevel = jsapConfig.getInt("complianceLevel");
		this.repairStrategyClassname = jsapConfig.getString("repairStrategy");

		return true;
	}

	private void initJSAP() throws JSAPException {
		FlaggedOption sourceOpt = new FlaggedOption("source");
		sourceOpt.setRequired(true);
		sourceOpt.setAllowMultipleDeclarations(false);
		sourceOpt.setLongFlag("source");
		sourceOpt.setShortFlag('s');
		sourceOpt.setStringParser(JSAP.STRING_PARSER);
		sourceOpt.setList(true);
		sourceOpt.setHelp("Define the path to the source code of the project.");
		jsap.registerParameter(sourceOpt);

		FlaggedOption classpathOpt = new FlaggedOption("classpath");
		classpathOpt.setRequired(true);
		classpathOpt.setAllowMultipleDeclarations(false);
		classpathOpt.setLongFlag("classpath");
		classpathOpt.setShortFlag('c');
		classpathOpt.setStringParser(JSAP.STRING_PARSER);
		classpathOpt.setHelp("Define the classpath of the project.");
		jsap.registerParameter(classpathOpt);

		FlaggedOption testOpt = new FlaggedOption("test");
		testOpt.setRequired(true);
		testOpt.setAllowMultipleDeclarations(false);
		testOpt.setLongFlag("test");
		testOpt.setShortFlag('t');
		testOpt.setList(true);
		testOpt.setStringParser(JSAP.STRING_PARSER);
		testOpt.setHelp("Define the test classes of the project to take into account (both failing and passing), fully-qualified class names, separated with ':' (even if the classpath contains other tests, only those are considered.");
		jsap.registerParameter(testOpt);

		FlaggedOption complianceLevelOpt = new FlaggedOption("complianceLevel");
		complianceLevelOpt.setRequired(false);
		complianceLevelOpt.setAllowMultipleDeclarations(false);
		complianceLevelOpt.setLongFlag("complianceLevel");
		complianceLevelOpt.setStringParser(JSAP.INTEGER_PARSER);
		complianceLevelOpt.setDefault("7");
		complianceLevelOpt.setHelp("The compliance level of the project.");
		jsap.registerParameter(complianceLevelOpt);

		FlaggedOption iterationOpt = new FlaggedOption("iteration");
		iterationOpt.setRequired(false);
		iterationOpt.setAllowMultipleDeclarations(false);
		iterationOpt.setLongFlag("iteration");
		iterationOpt.setShortFlag('i');
		iterationOpt.setStringParser(JSAP.INTEGER_PARSER);
		iterationOpt.setDefault("100");
		iterationOpt.setHelp("The maximum number of npefix iteration.");
		jsap.registerParameter(iterationOpt);

		FlaggedOption outputFolder = new FlaggedOption("workingdirectory");
		outputFolder.setRequired(false);
		outputFolder.setAllowMultipleDeclarations(false);
		outputFolder.setLongFlag("workingdirectory");
		outputFolder.setShortFlag('w');
		outputFolder.setStringParser(JSAP.STRING_PARSER);
		outputFolder.setDefault(".");
		outputFolder.setHelp("Define the location where npefix will put its files.");
		jsap.registerParameter(outputFolder);

		FlaggedOption repairStrategy = new FlaggedOption("repairStrategy");
		repairStrategy.setRequired(false);
		repairStrategy.setAllowMultipleDeclarations(false);
		repairStrategy.setLongFlag("repairStrategy");
		repairStrategy.setStringParser(JSAP.STRING_PARSER);
		repairStrategy.setDefault(DefaultRepairStrategy.class.getCanonicalName());
		repairStrategy.setHelp("Define the repair strategy used by NPEFix.");
		jsap.registerParameter(repairStrategy);
	}
}
