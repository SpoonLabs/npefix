package sacha.impl;

import sacha.classloader.enrich.EnrichableClassloader;
import sacha.finder.main.TestClassFinder;
import sacha.finder.main.TestInFolder;
import sacha.interfaces.IRunner;
import sacha.interfaces.ITestResult;

public class TestRunnerCore extends AbstractConfigurator implements IRunner {

	@Override
	public ITestResult runAllTestsInClasspath() {
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);

		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();
		return new sacha.runner.main.TestRunner(tests).run();
	}

	@Override
	public ITestResult runAllTestsInDirectory(String dir) {

		TestInFolder tf = new TestInFolder(dir);
		
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);
		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();
		
		return new sacha.runner.main.TestRunner(tf.find()).run();
	}
}
