package utils.sacha.finder.main;

import utils.sacha.finder.classes.impl.ClassloaderFinder;
import utils.sacha.finder.filters.impl.TestFilter;
import utils.sacha.finder.processor.Processor;

import java.net.URLClassLoader;

public class TestInURLClassloader{
	
	private URLClassLoader urlClassloader;

	public TestInURLClassloader(URLClassLoader classloader) {
		this.urlClassloader=classloader;
	}

	public Class<?>[] find(){
		return new Processor(new ClassloaderFinder(urlClassloader), new TestFilter()).process();
	}

}
