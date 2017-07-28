package fr.inria.spirals.npefix.transformer;

import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.main.all.TryCatchRepairStrategy;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.selector.ExplorerSelector;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class TryCatchRepairModelTest {

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDirectory(new File("target/instrumented"));
		FileUtils.deleteDirectory(new File("target/test-classes/instrumented"));
		FileUtils.deleteDirectory(new File("target/test-classes/foo/target"));
		FileUtils.deleteDirectory(new File("spooned-classes"));
		final File[] files = new File("target/test-classes/foo/").listFiles();
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getAbsolutePath());
			if (files[i].getAbsolutePath().endsWith(".class"))
				files[i].delete();
		}
		final File[] classFiles = new File("target/test-classes/").listFiles();
		for (int i = 0; i < classFiles.length; i++) {
			if (classFiles[i].getAbsolutePath().endsWith(".class"))
				classFiles[i].delete();
		}
		CallChecker.clear();
	}

	@Test
	public void testRepairWithTryCatch() throws Exception {
		URL sourcePath = getClass().getResource("/bar/src/main/java/Coneflower.java");
		URL testPath = getClass().getResource("/bar/src/test/java/ConeflowerTest.java");
		URL rootPath = getClass().getResource("/bar/");
		Launcher launcher = new Launcher(
				new String[]{sourcePath.getFile(),
						testPath.getFile()},
				rootPath.getFile() + "/../instrumented",
				rootPath.getFile() + "",
				rootPath.getFile(),
				new TryCatchRepairStrategy());

		launcher.instrument();

		NPEOutput results = launcher.run(new ExplorerSelector(new Strat4(ReturnType.NULL)));
		Assert.assertEquals("Strat4 Null failing", 4, results.getFailureCount());

		results = launcher.run(new ExplorerSelector(new Strat4(ReturnType.VAR)));
		Assert.assertEquals("Strat4 var failing", 3, results.getFailureCount());

		results = launcher.run(new ExplorerSelector(new Strat4(ReturnType.NEW)));
		Assert.assertEquals("Strat4 new failing", 0, results.getFailureCount());

		results = launcher.run(new ExplorerSelector(new Strat4(ReturnType.VOID)));
		Assert.assertEquals("Strat4 void failing", 4, results.getFailureCount());
	}
}
