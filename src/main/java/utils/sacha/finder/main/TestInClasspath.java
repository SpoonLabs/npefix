package utils.sacha.finder.main;

import utils.sacha.finder.classes.impl.ClasspathFinder;
import utils.sacha.finder.filters.impl.TestFilter;
import utils.sacha.finder.processor.Processor;

public class TestInClasspath{

	public Class<?>[] find(){
		return new Processor(new ClasspathFinder(), new TestFilter()).process();
	}

}
