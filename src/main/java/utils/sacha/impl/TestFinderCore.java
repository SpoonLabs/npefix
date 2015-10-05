package utils.sacha.impl;

import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.finder.main.TestClassFinder;

public class TestFinderCore extends AbstractConfigurator {

	public Class<?>[] findTestClasses() {
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);
		
		return new TestClassFinder(eClassloader).findTestClasses();
	}

}
