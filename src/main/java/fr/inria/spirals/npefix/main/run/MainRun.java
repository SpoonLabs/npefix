package fr.inria.spirals.npefix.main.run;

import java.util.Collections;
import java.util.List;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.Strategy;
import fr.inria.spirals.npefix.resi.strategies.*;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import utils.sacha.impl.TestRunnerCore;
import utils.sacha.interfaces.ITestResult;

public class MainRun {

	public static void main(String[] args) {
		// String[] toto = new String[]{};
		//
		// System.out.println(toto[0]);

		TestRunnerCore runner = new TestRunnerCore();
		runner.setEclipseMetadataFolder("/home/thomas/workspace/.metadata/");
		// runner.setEclipseProject("commons-math4-o");
		runner.setEclipseProject("spojo-core-o");
		// runner.setEclipseProject("01-Mckoi-o");
		// runner.setEclipseProject("02-freemarker-o");
		// runner.setEclipseProject("03-jfreechart-o");
		// runner.setEclipseProject("collections-331-o");
		// runner.setEclipseProject("lang-304-o");
		// runner.setEclipseProject("lang-587-o");
		// runner.setEclipseProject("lang-703-o");
		// runner.setEclipseProject("math-290-o");
		// runner.setEclipseProject("math-305-o");
		// runner.setEclipseProject("math-369-o");
		// runner.setEclipseProject("math-988a-o");
		// runner.setEclipseProject("math-988b-o");
		// runner.setEclipseProject("math-1115-o");
		// runner.setEclipseProject("math-1117-o");

		CallChecker.strat = new NoStrat();
		ITestResult res = runner.runAllTestsInClasspath();
		
		// runs the test folder
		res = runStrategy(runner, new NoStrat());
		boolean noStrat = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat1A();
		res = runStrategy(runner, new Strat1A());
		boolean s1a = res.getNbFailedTests() == 0;


		res = runStrategy(runner, new Strat1B());
		boolean s1b = res.getNbFailedTests() == 0;

		res = runStrategy(runner, new Strat2A());
		boolean s2a = res.getNbFailedTests() == 0;

		res = runStrategy(runner, new Strat2B());
		boolean s2b = res.getNbFailedTests() == 0;

		res = runStrategy(runner, new Strat3());
		boolean s3 = res.getNbFailedTests() == 0;

		res = runStrategy(runner, new Strat4(ReturnType.NULL));
		boolean s4a = res.getNbFailedTests() == 0;

		res = runStrategy(runner, new Strat4(ReturnType.NEW));
		boolean s4b = res.getNbFailedTests() == 0;

		res = runStrategy(runner, new Strat4(ReturnType.VAR));
		boolean s4c = res.getNbFailedTests() == 0;

		System.out.println("base -> " + noStrat);
		System.out.println("s1a -> " + s1a);
		System.out.println("s1b -> " + s1b);
		System.out.println("s2a -> " + s2a);
		System.out.println("s2b -> " + s2b);
		System.out.println("s3 -> " + s3);
		System.out.println("s4a -> " + s4a);
		System.out.println("s4b -> " + s4b);
		System.out.println("s4c -> " + s4c);
	}
	
	private static ITestResult runStrategy(TestRunnerCore runner, Strategy strategy) {
		CallChecker.strat = strategy;
		
		ITestResult res = runner.runAllTestsInClasspath();
		Result result = res.getResult();
		List<Failure> failures = result.getFailures();
		int countError = 0;
		int countFailure = 0;
		int countNPEError = 0;
		result.getIgnoreCount();
		for (int i = 0; i < failures.size(); i++) {
			Failure failure = failures.get(i);
			Throwable exception = failure.getException();
			if(exception != null) {
				if(exception instanceof NullPointerException) {
					countNPEError++;
				} else if (exception instanceof AssertionError){
					countFailure ++;
					//System.err.println(exception);
				} else {
					countError++;
					System.err.println(exception);
					System.err.println(exception.getStackTrace()[2]);
					if(exception.getCause() != null) {
						System.err.println(exception.getCause());
						System.err.println(exception.getCause().getStackTrace()[0]);
					}
				}
			}
		}
		String output = String.format("%s -> %d/%d (Error %d, NPEError %d, Failure %d) (%d ms)",
				strategy.getClass().getSimpleName(),
				res.getNbFailedTests(),
				res.getNbRunTests(),
				countError,
				countNPEError,
				countFailure,
				result.getRunTime());
		
		System.out.println(output);
		return res;
	}
}
