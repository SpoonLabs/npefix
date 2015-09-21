package sacha.mains;

import sacha.impl.TestRunnerCore;
import sacha.interfaces.ITestResult;

/** Runs all tests of an Eclipse project */
public class TestRunnerMain  {

	public static void main(String[] args) {
		TestRunnerCore runner = new TestRunnerCore();
		runner.setEclipseMetadataFolder("/home/bcornu/workspace/.metadata/");
		runner.setEclipseProject("joda-time");
		// runs the test folder
		ITestResult res = runner.runAllTestsInClasspath();
		System.out.println(res.getNbRunTests());
		System.out.println(res.getNbFailedTests());

	}

}
