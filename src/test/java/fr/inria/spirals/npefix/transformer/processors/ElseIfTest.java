package fr.inria.spirals.npefix.transformer.processors;

import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;



public class ElseIfTest {

	@Test
	public void elseIfTest() {
		Launcher spoon = new Launcher();
		spoon.addInputResource("src/test/resources/foo/src/main/java/");
		spoon.addProcessor(IfSplitter.class.getCanonicalName());
		spoon.addProcessor(CheckNotNull.class.getCanonicalName());
		spoon.setSourceOutputDirectory("target/instrumented");

		spoon.run();

		CtClass<Object> fooTernary = spoon.getFactory().Class().get("Foo");
		CtMethod<Object> elseIf = fooTernary.getMethod("elseIf");
		CtIf ctIf = elseIf.getBody().getStatement(0);
		CtBinaryOperator operator = (CtBinaryOperator) ctIf.getCondition();
		Assert.assertNotNull(operator.getLeftHandOperand().toString());
		Assert.assertNotNull(operator.getRightHandOperand().toString());
		CtStatement ctthenstatement = ctIf.getThenStatement();
		CtStatement ctelsestatement = (CtStatement) ((CtBlock) ctIf.getElseStatement()).getStatement(0);
		Assert.assertFalse(ctthenstatement instanceof CtAssignment);
		Assert.assertFalse(ctelsestatement instanceof CtAssignment);
	}
}
