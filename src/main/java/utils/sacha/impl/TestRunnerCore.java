package utils.sacha.impl;

import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.finder.main.TestClassFinder;
import utils.sacha.finder.main.TestInFolder;
import utils.sacha.interfaces.IRunner;
import utils.sacha.interfaces.ITestResult;
import utils.sacha.runner.main.TestRunner;

import java.util.HashSet;
import java.util.Set;

public class TestRunnerCore extends AbstractConfigurator implements IRunner {

	@Override
	public ITestResult runAllTestsInClasspath() {
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);

		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();
		Set<Class> testList = new HashSet();
		for (int i = 0; i < tests.length; i++) {
			String s = tests[i].getCanonicalName();
			if(s.startsWith("fr.inria.spirals.npefix")) {
				continue;
			}
			testList.add(tests[i]);
		}
		return new TestRunner().run(testList.toArray(new Class[]{}));
	}

	@Override
	public ITestResult runAllTestsInDirectory(String dir) {

		TestInFolder tf = new TestInFolder(dir);
		
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);
		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();
		
		return new TestRunner().run(tf.find());
	}
}
