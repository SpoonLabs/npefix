package utils.sacha.mains;

import utils.sacha.impl.GeneralToJavaCore;


/**  Manipulates Eclipse projects (nature, classpath, etc.  **/
public class EclipseGeneralToJavaProject {

	public static void main(String[] args) {
		GeneralToJavaCore core = new GeneralToJavaCore();
		core.setEclipseMetadataFolder("/home/bcornu/workspace/.metadata");
		core.setEclipseProject("jmeter-maven-plugin");		
		core.changeToJava();
	}
}
