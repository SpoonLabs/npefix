package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

public class LambdaTest {

	@Test
	public void test() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/Lamda.java");
		spoon.addProcessor(BeforeDerefAdder.class.getCanonicalName());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();


		CtClass<Object> fooTernary = spoon.getFactory().Class().get("Lamda");
		System.out.println(fooTernary);
	}
}
