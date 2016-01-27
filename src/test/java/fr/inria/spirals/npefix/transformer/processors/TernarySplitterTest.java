package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

/**
 * Split if condition into several if in order to add check not null before each section of the condition
 */
public class TernarySplitterTest {

	@Test
	public void test() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(TernarySplitter.class.getCanonicalName());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();

		CtClass<Object> fooTernary = spoon.getFactory().Class().get("FooTernary");
		Assert.assertNotNull(fooTernary);
	}
}
