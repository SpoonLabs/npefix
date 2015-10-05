package utils.sacha.finder.classes.impl;

import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.finder.classes.ClassFinder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProjectFinder implements ClassFinder {

	private EnrichableClassloader urlClassloader;
	public ProjectFinder(EnrichableClassloader urlClassloader) {
		this.urlClassloader = urlClassloader;
	}

	@Override
	public String[] getClasses() {
		List<String> classes = new ArrayList<>();
		for (URL url : urlClassloader.getURLs()) {
			if(new File(url.getPath()).isDirectory())
				classes.addAll(SourceFolderFinder.getClassesLoc(new File(url.getPath()), null));	
		}
		return classes.toArray(new String[0]);
	}

}
