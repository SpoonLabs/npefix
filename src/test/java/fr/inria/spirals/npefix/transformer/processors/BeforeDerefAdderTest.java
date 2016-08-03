package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtClass;

public class BeforeDerefAdderTest {

	@Test
	@Ignore
	public void genericTypeInInvocationType() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(BeforeDerefAdder.class.getCanonicalName());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();

		CtClass<Object> fooTernary = spoon.getFactory().Class().get("Foo");
		CtIf beforeDerefIf1 = fooTernary.getMethod("genericInMethodType").getBody().getStatement(1);
		CtLocalVariable NPEFixVariable = (CtLocalVariable) ((CtBlock) beforeDerefIf1.getThenStatement()).getStatement(0);
		Assert.assertEquals("java.lang.String", NPEFixVariable.getType().toString());


		CtIf beforeDerefIf2 = fooTernary.getMethod("genericWithoutExtendsInMethodType").getBody().getStatement(1);
		NPEFixVariable = (CtLocalVariable) ((CtBlock) beforeDerefIf2.getThenStatement()).getStatement(0);
		System.out.println(NPEFixVariable);
		Assert.assertEquals("T", NPEFixVariable.getType().toString());


	}
}
