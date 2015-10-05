package utils.sacha.finder.main;

import utils.sacha.finder.classes.impl.SourceFolderFinder;
import utils.sacha.finder.filters.impl.TestFilter;
import utils.sacha.finder.processor.Processor;

public class TestMain extends Main{

	public static Class<?>[] findTest(String testFolder){
		return getTestsClasses(checkFolder(testFolder));
	}
	
	private static Class<?>[] getTestsClasses(String testFolder) {
		return new Processor(new SourceFolderFinder(testFolder), new TestFilter()).process();
	}
	

	

}
