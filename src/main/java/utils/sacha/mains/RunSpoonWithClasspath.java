package utils.sacha.mains;

import utils.sacha.impl.DefaultSpooner;
import utils.sacha.interfaces.ISpooner;

/** Runs spoon in an easy manner */
public class RunSpoonWithClasspath {

	public static void main(String[] args) {
		
		ISpooner spooner = new DefaultSpooner();
		//project config
		spooner.setEclipseProject("test");
		spooner.setEclipseMetadataFolder("/home/bcornu/workspace/.metadata");
		//spoon config
		spooner.setSourceFolder("src");
//		spooner.setProcessors("bcu.transformer.processors.ClassAnnotation","bcu.transformer.processors.TryEncapsulation");
		spooner.setOutputFolder("/home/bcornu/workspace/test-spooned/src");

		spooner.setGraphicalOutput(true);
		
		spooner.spoon();
	}

}
