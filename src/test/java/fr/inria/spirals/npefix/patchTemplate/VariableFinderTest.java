package fr.inria.spirals.npefix.patchTemplate;

import fr.inria.spirals.npefix.patchTemplate.testClasses.ChildClassSamePackage;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtNewClassImpl;
import spoon.support.reflect.declaration.CtMethodImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VariableFinderTest {

	@Test
	public void findVariable() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get(ChildClassSamePackage.class);

		CtMethod statement = (CtMethod) foo.getMethodsByName("m").get(0);
		CtExpression m = ((CtInvocationImpl) ((CtMethodImpl) ((CtNewClassImpl) ((CtBlock) ((CtForEach) statement
				.getBody().getStatement(2)).getBody()).getStatement(0))
				.getAnonymousClass().getMethodsByName("m").get(0)).getBody()
				.getStatement(0)).getTarget();
		VariableFinder variableFinder = new VariableFinder(m);
		List<CtVariable> variables = variableFinder.find();
		List<String> variableNames = new ArrayList<>();
		for (int i = 0; i < variables.size(); i++) {
			CtVariable ctVariable = variables.get(i);
			variableNames.add(ctVariable.getSimpleName());
		}
		Assert.assertTrue(variableNames.contains("parameterInner"));
		Assert.assertTrue(variableNames.contains("innerClass"));
		Assert.assertTrue(variableNames.contains("foreach"));
		Assert.assertTrue(variableNames.contains("localVariable"));
		Assert.assertTrue(variableNames.contains("array"));
		Assert.assertTrue(variableNames.contains("parameter"));
		Assert.assertTrue(variableNames.contains("defaultChildField"));
		Assert.assertTrue(variableNames.contains("protectedChildField"));
		Assert.assertTrue(variableNames.contains("privateChildField"));
		Assert.assertTrue(variableNames.contains("publicChildField"));
		Assert.assertTrue(variableNames.contains("defaultParentField"));
		Assert.assertTrue(variableNames.contains("protectedParentField"));
		Assert.assertTrue(variableNames.contains("publicParentField"));
		Assert.assertEquals(13, variableNames.size());
	}

	@Test
	@Ignore // because not same behaviour in NPEFix
	public void findVariableType() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get(ChildClassSamePackage.class);

		CtMethod statement = (CtMethod) foo.getMethodsByName("m").get(0);
		CtExpression m = ((CtInvocationImpl) ((CtMethodImpl) ((CtNewClassImpl) ((CtBlock) ((CtForEach) statement
				.getBody().getStatement(2)).getBody()).getStatement(0))
				.getAnonymousClass().getMethodsByName("m").get(0)).getBody()
				.getStatement(0)).getTarget();
		VariableFinder variableFinder = new VariableFinder(m);

		List<CtVariable> variables = variableFinder.find(foo.getFactory().Type().OBJECT);
		Assert.assertEquals(13, variables.size());

		variables = variableFinder.find(foo.getFactory().Type().STRING);
		Assert.assertEquals(10, variables.size());

		variables = variableFinder.find(foo.getFactory().Type().createReference(Number.class));
		Assert.assertEquals(2, variables.size());

		variables = variableFinder.find(foo.getFactory().Type().INTEGER);
		Assert.assertEquals(1, variables.size());

		variables = variableFinder.find(foo.getFactory().Type().INTEGER_PRIMITIVE);
		Assert.assertEquals(1, variables.size());

		variables = variableFinder.find(foo.getFactory().Type().DOUBLE);
		Assert.assertEquals(1, variables.size());

		variables = variableFinder.find(foo.getFactory().Type().DOUBLE_PRIMITIVE);
		Assert.assertEquals(1, variables.size());
	}


	private Launcher getSpoonLauncher() {
		String classpath = System.getProperty("java.class.path");

		Launcher launcher = new Launcher();
		launcher.addInputResource("src/test/java/fr/inria/spirals/npefix/patchTemplate/testClasses");
		SpoonModelBuilder compiler = launcher.getModelBuilder();
		compiler.setSourceClasspath(classpath.split(File.pathSeparator));
		launcher.buildModel();
		return launcher;
	}
}