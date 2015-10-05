package utils.sacha.interfaces;

public interface IRunner {

	/** setEclipseClassPath must have been called before */
	utils.sacha.interfaces.ITestResult runAllTestsInClasspath();

	/** dir must be valid source folder */
	utils.sacha.interfaces.ITestResult runAllTestsInDirectory(String dir);

}
