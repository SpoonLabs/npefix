package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonException;

/**
 * Split if condition into several if in order to add check not null before each section of the condition
 */
public class ImplicitCastCheckerTest {

	@Test
	public void test() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(new AddImplicitCastChecker());
		spoon.addProcessor(new BeforeDerefAdder());
		spoon.setSourceOutputDirectory("target/instrumented");
		spoon.getEnvironment().setShouldCompile(true);
		spoon.getModelBuilder().setSourceClasspath(System.getProperty("java.class.path").split(":"));
		try {
			spoon.run();
		} catch (SpoonException e) {
			Assert.fail("No compilation error.");
		}
	}
}
