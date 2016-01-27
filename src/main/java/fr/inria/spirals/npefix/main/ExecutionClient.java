package fr.inria.spirals.npefix.main;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Laps;
import fr.inria.spirals.npefix.resi.oracle.TestOracle;
import fr.inria.spirals.npefix.resi.selector.Selector;
import org.junit.runner.Request;
import org.junit.runner.Result;
import utils.sacha.runner.main.TestRunner;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ExecutionClient {

	public static void main(String[] args) {
		ExecutionClient executionClient = new ExecutionClient(args[0], args[1]);
		executionClient.run();
	}

	private String classTestName;
	private String testName;
	private int port = Config.CONFIG.getServerPort();

	public ExecutionClient(String classTestName, String testName) {
		this.classTestName = classTestName;
		this.testName = testName;
	}

	/**
	 * Get the selector instantiated in the RMI server.
	 * @return
	 */
	private Selector getSelector() {
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			return  (Selector) registry.lookup("Selector");
		} catch (Exception e) {
			// if the decision server is not available exit the execution
			throw new RuntimeException(e);
		}
	}

	private void run() {
		Selector selector = getSelector();
		Laps laps = new Laps(selector);
		laps.setTestClassName(classTestName);
		laps.setTestName(testName);
		CallChecker.currentLaps = laps;
		CallChecker.strategySelector = selector;
		CallChecker.currentClassLoader = getClass().getClassLoader();
		final TestRunner testRunner = new TestRunner();
		try {
			Class<?> testClass = getClass().forName(classTestName);
			final Request request = Request.method(testClass, testName);
			Result result = testRunner.run(request);
			laps.setOracle(new TestOracle(result));

			System.out.println(laps);

			selector.restartTest(laps);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
