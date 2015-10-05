package utils.sacha.finder.main;

import utils.sacha.finder.classes.impl.SourceFolderFinder;
import utils.sacha.finder.filters.impl.TestFilter;
import utils.sacha.finder.processor.Processor;

public class TestInFolder{
	
	private String testFolder = null;

	public TestInFolder(String testFolder) {
		this.testFolder=testFolder;
	}

	public Class<?>[] find(){
		return new Processor(new SourceFolderFinder(testFolder), new TestFilter()).process();
	}

}
