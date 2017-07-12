package fr.inria.spirals.npefix.transformer;

import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.main.all.RepairStrategy;
import fr.inria.spirals.npefix.main.all.TryCatchRepairStrategy;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.strategies.*;
import fr.inria.spirals.npefix.transformer.processors.*;
import org.junit.Assert;
import org.junit.Test;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessingManager;
import spoon.support.QueueProcessingManager;

import java.net.URL;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class TryCatchRepairModelTest {

	@Test
	public void test() throws Exception {
		URL sourcePath = getClass().getResource("/foo/src/main/java/Coneflower.java");
		URL testPath = getClass().getResource("/foo/src/test/java/ConeflowerTest.java");
		URL rootPath = getClass().getResource("/foo/");
		Launcher launcher = new Launcher(
				new String[]{sourcePath.getFile(),
						testPath.getFile()},
				rootPath.getFile() + "/../instrumented",
				rootPath.getFile() + "",
				rootPath.getFile(),
				new TryCatchRepairStrategy(6));

		launcher.instrument();

		System.out.println(launcher.getSpoon().getFactory().Class().get("Coneflower"));

		NPEOutput results = launcher.runStrategy(new Strat4(ReturnType.NULL));
		Assert.assertEquals("Strat4 Null failing", 3, results.getFailureCount());

		results = launcher.runStrategy(new Strat4(ReturnType.VAR));
		Assert.assertEquals("Strat4 var failing", 3, results.getFailureCount());

		results = launcher.runStrategy(new Strat4(ReturnType.NEW));
		Assert.assertEquals("Strat4 new failing", 0, results.getFailureCount());

		results = launcher.runStrategy(new Strat4(ReturnType.VOID));
		Assert.assertEquals("Strat4 void failing", 3, results.getFailureCount());
	}
}
