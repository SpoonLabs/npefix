package sacha.interfaces;

public interface IRunner {

	/** setEclipseClassPath must have been called before */
	ITestResult runAllTestsInClasspath();

	/** dir must be valid source folder */
	ITestResult runAllTestsInDirectory(String dir);

}
