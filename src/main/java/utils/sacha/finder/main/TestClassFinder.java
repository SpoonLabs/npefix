package utils.sacha.finder.main;

import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.finder.classes.impl.ProjectFinder;
import utils.sacha.finder.filters.impl.TestFilter;
import utils.sacha.finder.processor.Processor;

public class TestClassFinder{
	
	private EnrichableClassloader urlClassloader;

	public TestClassFinder(EnrichableClassloader classloader) {
		this.urlClassloader=classloader;
	}

	public Class<?>[] findTestClasses(){
		return new Processor(new ProjectFinder(urlClassloader), new TestFilter()).process();
	}

}
