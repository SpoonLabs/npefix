package sacha.impl;

import sacha.classloader.enrich.EnrichableClassloader;
import sacha.finder.main.TestClassFinder;

public class TestFinderCore extends AbstractConfigurator {

	public Class<?>[] findTestClasses() {
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);
		
		return new TestClassFinder(eClassloader).findTestClasses();
	}

}
