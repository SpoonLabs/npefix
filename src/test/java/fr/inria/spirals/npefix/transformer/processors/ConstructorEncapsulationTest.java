package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;

import static org.junit.Assert.assertEquals;

public class ConstructorEncapsulationTest {

	@Test
	public void test() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(new ConstructorEncapsulation());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();

		CtClass fooTernary = spoon.getFactory().Class().get("Foo");
		CtConstructor constructor = fooTernary.getConstructor();
		assertEquals("public Foo() {\n"
				+ "    super();\n"
				+ "    fr.inria.spirals.npefix.resi.context.ConstructorContext _bcornu_methode_context1 = new fr.inria.spirals.npefix.resi.context.ConstructorContext(Foo.class, 9, 167, 247);\n"
				+ "    try {\n"
				+ "        field = null;\n"
				+ "        array = null;\n"
				+ "    } finally {\n"
				+ "        _bcornu_methode_context1.methodEnd();\n"
				+ "    }\n"
				+ "}", constructor.toString());
	}
}
