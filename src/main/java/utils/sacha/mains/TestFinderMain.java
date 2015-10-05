package utils.sacha.mains;

import utils.sacha.impl.TestFinderCore;

/** Runs all tests of an Eclipse project */
public class TestFinderMain  {

	public static void main(String[] args) {
		TestFinderCore finder = new TestFinderCore();
		finder.setEclipseMetadataFolder("/home/bcornu/workspace/.metadata/");
		finder.setEclipseProject("joda-time");
		
		Class<?>[] tests = finder.findTestClasses();
		for (Class<?> clazz : tests) {
			System.out.println(clazz);;
		}

	}

}
