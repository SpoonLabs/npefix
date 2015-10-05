package utils.sacha.finder.classes.impl;

import utils.sacha.finder.classes.ClassFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ClasspathFinder implements ClassFinder{

	@Override
	public String[] getClasses() {
		String classPath = System.getProperty("java.class.path");
		return findClassesInRoots(splitClassPath(classPath)).toArray(new String[0]);
	}

	private List<String> splitClassPath(String classPath) {
		final String separator = System.getProperty("path.separator");
		return Arrays.asList(classPath.split(separator));
	}
	
	private List<String> findClassesInRoots(List<String> roots) {
		List<String> classes = new ArrayList<>();
		for (String root : roots) {
			if(new File(root).isDirectory())
				classes.addAll(utils.sacha.finder.classes.impl.SourceFolderFinder.getClassesLoc(new File(root), null));
		}
		return classes;
	}
}
