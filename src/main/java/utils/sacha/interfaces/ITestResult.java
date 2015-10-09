package utils.sacha.interfaces;

import org.junit.runner.Result;

public interface ITestResult {

	int getNbRunTests();

	int getNbFailedTests();

	Result getResult();

}
