package utils.sacha.impl;

import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.finder.main.TestClassFinder;
import utils.sacha.finder.main.TestInFolder;
import utils.sacha.interfaces.IRunner;
import utils.sacha.interfaces.ITestResult;
import utils.sacha.runner.main.TestRunner;

public class TestRunnerCore extends AbstractConfigurator implements IRunner {

	@Override
	public ITestResult runAllTestsInClasspath() {
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);

		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();
		return new TestRunner(tests).run();
	}

	@Override
	public ITestResult runAllTestsInDirectory(String dir) {

		TestInFolder tf = new TestInFolder(dir);
		
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);
		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();
		
		return new TestRunner(tf.find()).run();
	}
}
