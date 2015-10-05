package fr.inria.spirals.npefix.main.run;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.strategies.*;
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
		// runs the test folder
		System.out.print("sans strat -> ");
		ITestResult res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests() + "/" + res.getNbRunTests());
		boolean noStrat = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat1A();
		System.out.print("s1a -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s1a = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat1B();
		System.out.print("s1b -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s1b = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat2A();
		System.out.print("s2a -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s2a = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat2B();
		System.out.print("s2b -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s2b = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat3();
		System.out.print("s3 -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s3 = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat4(ReturnType.NULL);
		System.out.print("s4a -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s4a = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat4(ReturnType.NEW);
		System.out.print("s4b -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
		boolean s4b = res.getNbFailedTests() == 0;

		CallChecker.strat = new Strat4(ReturnType.VAR);
		System.out.print("s4c -> ");
		res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbFailedTests());
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
}
