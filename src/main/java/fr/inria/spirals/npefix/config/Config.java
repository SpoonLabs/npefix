package fr.inria.spirals.npefix.config;

import java.io.IOException;
import java.util.Properties;

public class Config  {
	public static Config CONFIG = new Config();
	private String datasetRoot;
	private double greedyEpsilon;
	private int randomSeed;
	private int serverPort;
	private int nbIteration;
	private int timeoutIteration;
	private Properties properties = new Properties();
	private String workingDirectory;
	private String m2Repository;

	private Config() {
		try {
			properties.load(getClass().getResource("/config.ini").openStream());
			timeoutIteration = Integer.parseInt(properties.getProperty("iteration.timeout"));
			this.nbIteration = Integer.parseInt(properties.getProperty("iteration.count"));
			this.serverPort = Integer.parseInt(properties.getProperty("server.port"));
			this.randomSeed = Integer.parseInt(properties.getProperty("random.seed"));
			this.greedyEpsilon = Double.parseDouble(properties.getProperty("selector.greedy.epsilon"));
			this.datasetRoot = properties.getProperty("evaluation.datasetRoot");
			this.workingDirectory = properties.getProperty("evaluation.workingDirectory");
			this.m2Repository = properties.getProperty("evaluation.m2Root").replaceFirst("^~", System.getProperty("user.home")) + "repository/";
		} catch (IOException e) {
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

	public int getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
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
		this.datasetRoot = datasetRoot;
	}

	public String getEvaluationWorkingDirectory() {
		return workingDirectory;
	}

	public void setEvaluationWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getM2Repository() {
		return m2Repository;
	}

	public void setM2Repository(String m2Repository) {
		this.m2Repository = m2Repository.replaceFirst("^~", System.getProperty("user.home")) + "repository/";
	}
}
