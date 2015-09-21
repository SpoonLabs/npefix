package sacha.mains;

import java.io.File;
import java.io.PrintStream;

import sacha.impl.TestSuiteCreatorCore;

/** Generates on standard output a JUnit4 test class that runs all the test of a given Eclipse project */
public class TestSuiteGeneratorMain {

	public static void main(String[] args) {
		PrintStream f;
		TestSuiteCreatorCore tcsc = new TestSuiteCreatorCore();
		tcsc.setEclipseMetadataFolder("/home/langloisj/workspace/.metadata");
		tcsc.setEclipseProject("jbehave-core");
		try
		{
			f = new PrintStream(new File("/home/langloisj/eclipse-workspace-projects-with-junit-tests/jbehave-core/src/test/java/AllTests.java"));	
		}catch(Exception e){throw new RuntimeException(e);}
		tcsc.printJavaTestSuite(f,"AllTests");
	}
	
}

