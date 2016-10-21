package fr.inria.spirals.npefix.main;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.oracle.ExceptionOracle;
import fr.inria.spirals.npefix.resi.oracle.TestOracle;
import fr.inria.spirals.npefix.resi.selector.Selector;
import org.junit.runner.Request;
import org.junit.runner.Result;
import utils.sacha.runner.main.TestRunner;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecutionClient {

	public static void main(String[] args) {
		ExecutionClient executionClient = new ExecutionClient(args[0], args[1]);
		Config.CONFIG.setRandomSeed(Integer.parseInt(args[2]));
		executionClient.run();
	}

	private String classTestName;
	private String testName;
	private int port = Config.CONFIG.getServerPort();
	private String host = Config.CONFIG.getServerHost();

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
			Registry registry = LocateRegistry.getRegistry(host, port);
			return  (Selector) registry.lookup("Selector");
		} catch (Exception e) {
			// if the decision server is not available exit the execution
			throw new RuntimeException(e);
		}
	}

	private void run() {
		Selector selector = getSelector();
		Lapse lapse = new Lapse(selector);
		lapse.setTestClassName(classTestName);
		lapse.setTestName(testName);

		try {
			if(!selector.startLaps(lapse)) {
				return;
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		//CallChecker.strategySelector = selector;
		CallChecker.currentClassLoader = getClass().getClassLoader();
		final TestRunner testRunner = new TestRunner();
		try {
			Class<?> testClass = getClass().forName(classTestName);
			final Request request = Request.method(testClass, testName);

			ExecutorService executor = Executors.newSingleThreadExecutor();

			final Future<Result> handler = executor.submit(new Callable<Result>() {
				@Override
				public Result call() throws Exception {
					return testRunner.run(request);
				}
			});

			try {
				executor.shutdownNow();
				Result result = handler.get(25, TimeUnit.SECONDS);
				lapse = getSelector().getCurrentLapse();
				lapse.setOracle(new TestOracle(result));
			} catch (TimeoutException e) {
				lapse = getSelector().getCurrentLapse();
				lapse.setOracle(new ExceptionOracle(e));
				e.printStackTrace();
				handler.cancel(true);
			} catch (ExecutionException e) {
				lapse = getSelector().getCurrentLapse();
				lapse.setOracle(new ExceptionOracle(e));
				e.printStackTrace();
				handler.cancel(true);
			}


			selector.restartTest(lapse);
			System.out.println(lapse);
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
