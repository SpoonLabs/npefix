package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Test;
import spoon.Launcher;

public class CheckNotNullTest {

	@Test
	public void test() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(new CheckNotNull());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();
	}
}
