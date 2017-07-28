package fr.inria.spirals.npefix.transformer;

import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.main.all.TryCatchRepairStrategy;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.selector.ExplorerSelector;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

		NPEOutput results = launcher.run(new ExplorerSelector(new NoStrat()));
		Assert.assertEquals(new HashSet<>(), getPassingTest(results));
		Assert.assertEquals("NoStrat failing", results.size(), results.getFailureCount());

		results = launcher.run(1500, new ExplorerSelector(new Strat4(ReturnType.NULL)));
		Assert.assertEquals(new HashSet<String>(), getPassingTest(results));
		Assert.assertTrue(results.size() > 0);
		Assert.assertEquals("Strat4 Null failing", results.size(), results.getFailureCount());

		results = launcher.run(1500, new ExplorerSelector(new Strat4(ReturnType.VAR)));
		Assert.assertEquals(new HashSet<String>(Arrays.asList("ConeflowerTest#testThrowException")), getPassingTest(results));
		Assert.assertTrue(results.size() > 0);
		Assert.assertEquals("Strat4 var passing", 1, results.size() - results.getFailureCount());
		Assert.assertEquals("Strat4 var failing", 3, results.getFailureCount());

		results = launcher.run(1500, new ExplorerSelector(new Strat4(ReturnType.NEW)));
		Assert.assertTrue(results.size() > 0);
		Assert.assertEquals("Strat4 new failing", 8, results.getFailureCount());

		results = launcher.run(1500, new ExplorerSelector(new Strat4(ReturnType.VOID)));
		Assert.assertTrue(results.size() == 0);
		Assert.assertEquals("Strat4 void failing", results.size(), results.getFailureCount());
	}

	private Set<String> getPassingTest(NPEOutput output) {
		Set<String> passingTests = new HashSet<>();
		for (Lapse lapse : output) {
			if (lapse.getOracle().isValid()) {
				passingTests.add(lapse.getTestClassName() + "#" + lapse.getTestName());
			}
		}
		return passingTests;
	}
}
