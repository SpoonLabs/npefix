package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class TargetModifierTest {

	@Test
	public void beforeCallInMultiCatch() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(TargetModifier.class.getCanonicalName());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();

		CtClass<Object> fooTernary = spoon.getFactory().Class().get("Foo");
		CtMethod<Object> multiCatch = fooTernary.getMethod("multiCatch");
		System.out.println(multiCatch);
		CtTry ctTry = multiCatch.getBody().getStatement(0);
		CtCatch ctCatch = ctTry.getCatchers().get(0);
		Assert.assertFalse(ctCatch.getBody().getStatement(0) instanceof CtAssignment);
		// catch variable cannot be null
		Assert.assertEquals("e.printStackTrace()", ctCatch.getBody().getStatement(0).toString());
	}
}
