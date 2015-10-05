package utils.sacha.mains;

import utils.sacha.project.utils.MavenModulesMerger;

/** Merges Maven projects in one single Eclipse project */
public class MergeMavenProjects {

	public static void main(String[] args) {
		MavenModulesMerger merger = new MavenModulesMerger();
		merger.setEclipseMetadataFolder("/home/bcornu/workspace/.metadata");
		merger.setEclipseProject("find-sec-bugs");
		merger.merge();
	}
}
