package fr.inria.spirals.npefix.patchTemplate;

import fr.inria.spirals.npefix.patchTemplate.template.SkipMethodReturn;
import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.net.URL;

public class SkipMethodReturnTest {

	@Test
	public void skipMethodReturnInConstructorTest() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get("Foo2");

		CtInvocation statement = (CtInvocation) foo.getConstructor().getBody().getStatement(1);
		Assert.assertEquals("public Foo2() {\n"
				+ "    super();\n"
				+ "    field.toString();\n"
				+ "    field = null;\n"
				+ "    array = null;\n"
				+ "}", foo.getConstructor().toString());

		new SkipMethodReturn().apply(statement.getTarget());

		Assert.assertEquals("public Foo2() {\n"
				+ "    super();\n"
				+ "    if ((field) != null) {\n"
				+ "        field.toString();\n"
				+ "        field = null;\n"
				+ "        array = null;\n"
				+ "    }\n"
				+ "}", foo.getConstructor().toString());
	}


	@Test
	public void skipLineTest() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get("Foo");

		CtForEach afor = (CtForEach) foo.getMethod("foo2").getBody().getStatement(1);
		Assert.assertEquals("for (java.lang.String element : array) {\n"
				+ "    result += element.toString();\n"
				+ "    if (element == null) {\n"
				+ "        return null;\n"
				+ "    }\n"
				+ "}", afor.toString());

		new SkipMethodReturn().apply(afor.getExpression());

		Assert.assertEquals("if ((array) == null)\n"
				+ "    return null;\n", foo.getMethod("foo2").getBody().getStatement(1).toString());
	}

	private Launcher getSpoonLauncher() {
		URL sourcePath = getClass().getResource("/foo/src/main/java/");
		URL testPath = getClass().getResource("/foo/src/test/java/");
		String classpath = System.getProperty("java.class.path");

		Launcher launcher = new Launcher();
		launcher.addInputResource(sourcePath.getPath());
		launcher.addInputResource(testPath.getPath());
		SpoonModelBuilder compiler = launcher.getModelBuilder();
		compiler.setSourceClasspath(classpath.split(File.pathSeparator));
		launcher.buildModel();
		return launcher;
	}
}