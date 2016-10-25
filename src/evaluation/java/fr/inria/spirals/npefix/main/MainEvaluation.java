package fr.inria.spirals.npefix.main;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.NPEFixTemplateEvaluation;
import fr.inria.spirals.npefix.resi.selector.ExplorationSelectorEvaluation;
import fr.inria.spirals.npefix.resi.selector.GreedySelectorEvaluation;
import fr.inria.spirals.npefix.resi.selector.MonoExplorationEvaluation;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;

public abstract class MainEvaluation {
	private static final String WORKING_DIRECTORY = "workingDirectory";
	private static final String OUTPUT_DIRECTORY = "outputDirectory";
	private static final String GREEDY_EPSILON = "greedyEpsilon";
	private static final String RANDOM_SEED = "randomSeed";
	private static final String M2_REPO = "m2Repo";
	private static final String NB_LAPS = "nbLaps";
	private static final String TEST_TIMEOUT = "testTimeout";
	private static final String NPEDATASET = "npedataset";
	private static JSAP jsap = new JSAP();

	public static void main(String[] args) {
		System.out.println("CONFIGURATION:");
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			System.out.println(arg);
		}
		System.out.println("END CONFIGURATION\n");
		try {
			initJSAP();
			JSAPResult arguments = parseArguments(args);
			if (arguments == null) {
				return;
			}
			if(arguments.getString("mode").equals("normal")) {
				GreedySelectorEvaluation evaluation = new GreedySelectorEvaluation();
				evaluation.setup();
				Method project = GreedySelectorEvaluation.class.getMethod(arguments.getString("project").toLowerCase().replace("-", ""));
				project.invoke(evaluation);
			} else if(arguments.getString("mode").equals("exploration")) {
				ExplorationSelectorEvaluation evaluation = new ExplorationSelectorEvaluation();
				evaluation.setup();
				Method project = ExplorationSelectorEvaluation.class.getMethod(arguments.getString("project").toLowerCase().replace("-", ""));
				project.invoke(evaluation);
			} else if(arguments.getString("mode").equals("mono")) {
				MonoExplorationEvaluation evaluation = new MonoExplorationEvaluation();
				evaluation.setup();
				Method project = MonoExplorationEvaluation.class.getMethod(arguments.getString("project").toLowerCase().replace("-", ""));
				project.invoke(evaluation);
			} else if(arguments.getString("mode").equals("Template")) {
				NPEFixTemplateEvaluation evaluation = new NPEFixTemplateEvaluation();
				Method project = NPEFixTemplateEvaluation.class.getMethod(arguments.getString("project").toLowerCase().replace("-", ""));
				project.invoke(evaluation);
			}
		} catch (NoSuchMethodException e) {
			System.err.println("The project is not found");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void showUsage() {
		System.err.println();
		System.err.println("Usage: java -jar npefix.jar");
		System.err.println("                          " + jsap.getUsage());
		System.err.println();
		System.err.println(jsap.getHelp());
	}

	private static JSAPResult parseArguments(String[] args) {
		JSAPResult config = jsap.parse(args);
		if (!config.success()) {
			System.err.println();
			for (Iterator<?> errs = config.getErrorMessageIterator(); errs
					.hasNext(); ) {
				System.err.println("Error: " + errs.next());
			}
			showUsage();
			return null;
		}
		if (config.contains(GREEDY_EPSILON)) {
			Config.CONFIG.setGreedyEpsilon(config.getDouble(GREEDY_EPSILON));
		}
		if (config.contains(RANDOM_SEED)) {
			Config.CONFIG.setRandomSeed(config.getInt(RANDOM_SEED));
		}
		if (config.contains(WORKING_DIRECTORY)) {
			Config.CONFIG.setEvaluationWorkingDirectory(config.getString(WORKING_DIRECTORY));
		}
		if (config.contains(OUTPUT_DIRECTORY)) {
			Config.CONFIG.setOutputDirectory(config.getString(OUTPUT_DIRECTORY));
		}
		if (config.contains(M2_REPO)) {
			Config.CONFIG.setM2Repository(config.getString(M2_REPO));
		}
		if (config.contains(NB_LAPS)) {
			Config.CONFIG.setNbIteration(config.getInt(NB_LAPS));
		}
		if (config.contains(TEST_TIMEOUT)) {
			Config.CONFIG.setTimeoutIteration(config.getInt(TEST_TIMEOUT));
		}
		if (config.contains(NPEDATASET)) {
			Config.CONFIG.setDatasetRoot(new File(config.getString(NPEDATASET)).getAbsolutePath() + "/");
		}

		return config;
	}

	private static void initJSAP() throws JSAPException {
		FlaggedOption projectOpt = new FlaggedOption("project");
		projectOpt.setRequired(true);
		projectOpt.setAllowMultipleDeclarations(false);
		projectOpt.setLongFlag("project");
		projectOpt.setShortFlag('p');
		projectOpt.setUsageName("math-1117...");
		projectOpt.setStringParser(JSAP.STRING_PARSER);
		projectOpt.setHelp("The bug to execute.");
		jsap.registerParameter(projectOpt);

		FlaggedOption modeOpt = new FlaggedOption("mode");
		modeOpt.setRequired(false);
		modeOpt.setAllowMultipleDeclarations(false);
		modeOpt.setLongFlag("mode");
		modeOpt.setShortFlag('m');
		modeOpt.setUsageName("mode");
		modeOpt.setDefault("normal");
		modeOpt.setStringParser(JSAP.STRING_PARSER);
		modeOpt.setHelp("The execution mode (normal, exploration, mono).");
		jsap.registerParameter(modeOpt);

		FlaggedOption workingDirectoryOpt = new FlaggedOption(WORKING_DIRECTORY);
		workingDirectoryOpt.setRequired(false);
		workingDirectoryOpt.setAllowMultipleDeclarations(false);
		workingDirectoryOpt.setLongFlag("working");
		workingDirectoryOpt.setShortFlag('x');
		workingDirectoryOpt.setUsageName(WORKING_DIRECTORY);
		workingDirectoryOpt.setStringParser(JSAP.STRING_PARSER);
		workingDirectoryOpt.setHelp("The path to the evaluation working directory.");
		jsap.registerParameter(workingDirectoryOpt);

		FlaggedOption outputDirectoryOpt = new FlaggedOption(OUTPUT_DIRECTORY);
		outputDirectoryOpt.setRequired(false);
		outputDirectoryOpt.setAllowMultipleDeclarations(false);
		outputDirectoryOpt.setLongFlag("output");
		outputDirectoryOpt.setShortFlag('o');
		outputDirectoryOpt.setUsageName(WORKING_DIRECTORY);
		outputDirectoryOpt.setStringParser(JSAP.STRING_PARSER);
		outputDirectoryOpt.setHelp("The path to the evaluation output directory.");
		jsap.registerParameter(outputDirectoryOpt);

		FlaggedOption npeDataset = new FlaggedOption(NPEDATASET);
		npeDataset.setRequired(false);
		npeDataset.setAllowMultipleDeclarations(false);
		npeDataset.setLongFlag(NPEDATASET);
		npeDataset.setShortFlag('d');
		npeDataset.setUsageName(NPEDATASET);
		npeDataset.setStringParser(JSAP.STRING_PARSER);
		npeDataset.setHelp("The path to the npe dataset.");
		jsap.registerParameter(npeDataset);

		FlaggedOption m2Opt = new FlaggedOption(M2_REPO);
		m2Opt.setRequired(false);
		m2Opt.setAllowMultipleDeclarations(false);
		m2Opt.setLongFlag("m2");
		m2Opt.setShortFlag('k');
		m2Opt.setUsageName("~/.m2");
		m2Opt.setStringParser(JSAP.STRING_PARSER);
		m2Opt.setDefault("~/.m2");
		m2Opt.setHelp("The m2 folder");
		jsap.registerParameter(m2Opt);

		FlaggedOption epsilonOpt = new FlaggedOption(GREEDY_EPSILON);
		epsilonOpt.setRequired(false);
		epsilonOpt.setAllowMultipleDeclarations(false);
		epsilonOpt.setLongFlag("epsilon");
		epsilonOpt.setShortFlag('e');
		epsilonOpt.setUsageName("0.2");
		epsilonOpt.setStringParser(JSAP.DOUBLE_PARSER);
		epsilonOpt.setDefault("0.2");
		epsilonOpt.setHelp("The Epsilon-Greedy.");
		jsap.registerParameter(epsilonOpt);

		FlaggedOption randomSeedOpt = new FlaggedOption(RANDOM_SEED);
		randomSeedOpt.setRequired(false);
		randomSeedOpt.setAllowMultipleDeclarations(false);
		randomSeedOpt.setLongFlag("seed");
		randomSeedOpt.setShortFlag('s');
		randomSeedOpt.setStringParser(JSAP.INTEGER_PARSER);
		randomSeedOpt.setHelp("The seed of the random generator.");
		jsap.registerParameter(randomSeedOpt);

		FlaggedOption nbLapsOpt = new FlaggedOption(NB_LAPS);
		nbLapsOpt.setRequired(false);
		nbLapsOpt.setAllowMultipleDeclarations(false);
		nbLapsOpt.setLongFlag("laps");
		nbLapsOpt.setShortFlag('l');
		nbLapsOpt.setStringParser(JSAP.INTEGER_PARSER);
		nbLapsOpt.setDefault("100");
		nbLapsOpt.setHelp("Defines the number of laps.");
		jsap.registerParameter(nbLapsOpt);

		FlaggedOption testTiemoutOpt = new FlaggedOption(TEST_TIMEOUT);
		testTiemoutOpt.setRequired(false);
		testTiemoutOpt.setAllowMultipleDeclarations(false);
		testTiemoutOpt.setLongFlag("timeout");
		testTiemoutOpt.setShortFlag('t');
		testTiemoutOpt.setStringParser(JSAP.INTEGER_PARSER);
		testTiemoutOpt.setDefault("5");
		testTiemoutOpt.setHelp("The test timeout in second");
		jsap.registerParameter(testTiemoutOpt);
	}
}
