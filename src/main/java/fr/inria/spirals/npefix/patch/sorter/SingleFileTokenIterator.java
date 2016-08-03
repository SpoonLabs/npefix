package fr.inria.spirals.npefix.patch.sorter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * iterates over the probabilityPatch of size n in a given file
 */
public class SingleFileTokenIterator extends StringTokenIterator {
	public SingleFileTokenIterator(File f, int n) throws IOException {
		super(FileUtils.readFileToString(f), n);
		if (f.isDirectory()) {
			throw new RuntimeException("oops,should be a regular file");
		}
	}


}
