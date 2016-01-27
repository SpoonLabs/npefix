package fr.inria.spirals.npefix.config;

import java.io.IOException;
import java.util.Properties;

public class Config  {
	public static Config CONFIG = new Config();
	private Properties properties = new Properties();
	private Config() {
		try {
			properties.load(getClass().getResource("/config.ini").openStream());
		} catch (IOException e) {
			throw new RuntimeException("Unable to open the configuration.", e);
		}
	}

	public int getTimeoutIteration() {
		return Integer.parseInt(properties.getProperty("iteration.timeout"));
	}

	public int getNbIteration() {
		return Integer.parseInt(properties.getProperty("iteration.count"));
	}

	public int getServerPort() {
		return Integer.parseInt(properties.getProperty("server.port"));
	}

	public int getRandomSeed() {
		return Integer.parseInt(properties.getProperty("random.seed"));
	}

	public double getGreedyEpsilon() {
		return Double.parseDouble(properties.getProperty("selector.greedy.epsilon"));
	}

	public String getDatasetRoot() {
		return properties.getProperty("evaluation.datasetRoot");
	}

	public String getEvaluationWorkingDirectory() {
		return properties.getProperty("evaluation.workingDirectory");
	}

	public String getM2Repo() {
		return properties.getProperty("evaluation.m2Root").replaceFirst("^~", System.getProperty("user.home")) + "repository/";
	}
}
