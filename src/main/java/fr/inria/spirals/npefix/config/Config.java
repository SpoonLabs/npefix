package fr.inria.spirals.npefix.config;

import fr.inria.spirals.npefix.resi.RandomGenerator;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Config  {
	private static final String ITERATION_COUNT = "iteration.count";
	private static final String SERVER_PORT = "server.port";
	private static final String SERVER_HOST = "server.host";
	private static final String SERVER_NAME = "server.name";
	private static final String RANDOM_SEED = "random.seed";
	private static final String SELECTOR_GREEDY_EPSILON = "selector.greedy.epsilon";
	private static final String EVALUATION_DATASET_ROOT = "evaluation.datasetRoot";
	private static final String EVALUATION_WORKING_DIRECTORY = "evaluation.workingDirectory";
	private static final String EVALUATION_OUTPUT_DIRECTORY = "evaluation.outputDirectory";
	private static final String EVALUATION_M2_ROOT = "evaluation.m2Root";
	private static final String ITERATION_TIMEOUT = "iteration.timeout";

	public static Config CONFIG = new Config();
	private String outputDirectory;
	private String serverName;
	private String datasetRoot;
	private double greedyEpsilon;
	private int randomSeed;
	private int serverPort;
	private String serverHost;
	private int nbIteration;
	private int timeoutIteration;
	private Properties properties = new Properties();
	private String workingDirectory;
	private String m2Repository;
	private boolean multiPoints = true;

	private Config() {
		try {
			properties.load(getClass().getResource("/config.ini").openStream());

			this.timeoutIteration = Integer.parseInt(properties.getProperty(ITERATION_TIMEOUT));
			this.nbIteration = Integer.parseInt(properties.getProperty(ITERATION_COUNT));
			this.serverPort = Integer.parseInt(properties.getProperty(SERVER_PORT));
			this.serverHost = properties.getProperty(SERVER_HOST);
			this.randomSeed = Integer.parseInt(properties.getProperty(RANDOM_SEED));
			this.greedyEpsilon = Double.parseDouble(properties.getProperty(SELECTOR_GREEDY_EPSILON));
			this.datasetRoot = properties.getProperty(EVALUATION_DATASET_ROOT);
			this.workingDirectory = properties.getProperty(EVALUATION_WORKING_DIRECTORY);
			this.outputDirectory = properties.getProperty(EVALUATION_OUTPUT_DIRECTORY, "output/");
			setM2Repository(properties.getProperty(EVALUATION_M2_ROOT));
			this.serverName = properties.getProperty(SERVER_NAME, "Selector");

			File currentDir = new File(System.getProperty("user.dir") + "/config.ini");
			if (currentDir.exists()) {
				Properties userProperties = new Properties();
				userProperties.load(new FileReader(currentDir));

				this.timeoutIteration = Integer.parseInt(properties.getProperty(ITERATION_TIMEOUT, timeoutIteration + ""));
				this.nbIteration = Integer.parseInt(userProperties.getProperty(ITERATION_COUNT, nbIteration + ""));
				this.serverPort = Integer.parseInt(userProperties.getProperty(SERVER_PORT, serverPort + ""));
				this.serverHost = userProperties.getProperty(SERVER_HOST, serverHost);
				this.randomSeed = Integer.parseInt(userProperties.getProperty(RANDOM_SEED, randomSeed + ""));
				this.greedyEpsilon = Double.parseDouble(userProperties.getProperty(SELECTOR_GREEDY_EPSILON, greedyEpsilon + ""));
				this.datasetRoot = userProperties.getProperty(EVALUATION_DATASET_ROOT, datasetRoot);
				this.workingDirectory = userProperties.getProperty(EVALUATION_WORKING_DIRECTORY, workingDirectory);
				this.outputDirectory = userProperties.getProperty(EVALUATION_OUTPUT_DIRECTORY, outputDirectory);
				setM2Repository(userProperties.getProperty(EVALUATION_M2_ROOT, m2Repository));
				this.serverName = userProperties.getProperty(SERVER_NAME, serverName);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to open the configuration.", e);
		}

	}

	public int getTimeoutIteration() {
		return timeoutIteration;
	}

	public void setTimeoutIteration(int timeoutIteration) {
		this.timeoutIteration = timeoutIteration;
	}

	public int getNbIteration() {
		return nbIteration;
	}

	public void setNbIteration(int nbIteration) {
		this.nbIteration = nbIteration;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
		RandomGenerator.seed = randomSeed;
	}

	public double getGreedyEpsilon() {
		return greedyEpsilon;
	}

	public void setGreedyEpsilon(double greedyEpsilon) {
		this.greedyEpsilon = greedyEpsilon;
	}

	public String getDatasetRoot() {
		return datasetRoot;
	}

	public void setDatasetRoot(String datasetRoot) {
		if (datasetRoot.charAt(datasetRoot.length() -1) != '/') {
			datasetRoot += "/";
		}
		this.datasetRoot = datasetRoot;
	}

	public String getEvaluationWorkingDirectory() {
		return workingDirectory;
	}

	public void setEvaluationWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		if (outputDirectory.charAt(outputDirectory.length() -1) != '/') {
			outputDirectory += "/";
		}
		this.outputDirectory = outputDirectory;
	}

	public String getM2Repository() {
		return m2Repository;
	}

	public void setM2Repository(String m2Repository) {
		if (m2Repository.charAt(m2Repository.length() -1) != '/') {
			m2Repository += "/";
		}
		this.m2Repository = m2Repository.replaceFirst("^~", System.getProperty("user.home")) + "repository/";
	}

	public boolean extractInvocation() {
		return true;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public boolean isMultiPoints() {
		return multiPoints;
	}

	public void setMultiPoints(boolean multiPoints) {
		this.multiPoints = multiPoints;
	}
}
