package fr.inria.spirals.npefix.patchTemplate;

import fr.inria.spirals.npefix.patchTemplate.template.ReplaceLocal;
import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtForEach;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class ReplaceLocalTest {

	@Test
	public void ReplaceLocalInstanceTest() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get("Foo");

		CtForEach afor = (CtForEach) foo.getMethod("foo2").getBody().getStatement(1);
		Assert.assertEquals("for (String element : array) {\n"
				+ "    result += element.toString();\n"
				+ "    if (element == null) {\n"
				+ "        return null;\n"
				+ "    }\n"
				+ "}", afor.toString());

		new ReplaceLocal(foo.getFactory().Class().get(ArrayList.class)).apply(afor.getExpression());

		Assert.assertEquals("if ((array) == null)\n"
				+ "    for (String element : new ArrayList()) {\n"
				+ "        result += element.toString();\n"
				+ "        if (element == null) {\n"
				+ "            return null;\n"
				+ "        }\n"
				+ "    }\n"
				+ "else\n"
				+ "    for (String element : array) {\n"
				+ "        result += element.toString();\n"
				+ "        if (element == null) {\n"
				+ "            return null;\n"
				+ "        }\n"
				+ "    }\n", foo.getMethod("foo2").getBody().getStatement(1).toString());
	}

	@Test
	public void ReplaceLocalVariableTest() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get("Foo");

		CtForEach afor = (CtForEach) foo.getMethod("foo2").getBody().getStatement(1);
		Assert.assertEquals("for (String element : array) {\n"
				+ "    result += element.toString();\n"
				+ "    if (element == null) {\n"
				+ "        return null;\n"
				+ "    }\n"
				+ "}", afor.toString());

		new ReplaceLocal(foo.getField("field")).apply(afor.getExpression());

		Assert.assertEquals("if ((array) == null)\n"
				+ "    for (String element : this.field) {\n"
				+ "        result += element.toString();\n"
				+ "        if (element == null) {\n"
				+ "            return null;\n"
				+ "        }\n"
				+ "    }\n"
				+ "else\n"
				+ "    for (String element : array) {\n"
				+ "        result += element.toString();\n"
				+ "        if (element == null) {\n"
				+ "            return null;\n"
				+ "        }\n"
				+ "    }\n", foo.getMethod("foo2").getBody().getStatement(1).toString());
	}


	private Launcher getSpoonLauncher() {
		URL sourcePath = getClass().getResource("/foo/src/main/java/");
		URL testPath = getClass().getResource("/foo/src/test/java/");
		String classpath = System.getProperty("java.class.path");

		Launcher launcher = new Launcher();
		launcher.addInputResource(sourcePath.getPath());
		launcher.addInputResource(testPath.getPath());
		launcher.getEnvironment().setAutoImports(true);

		SpoonModelBuilder compiler = launcher.getModelBuilder();
		compiler.setSourceClasspath(classpath.split(File.pathSeparator));
		launcher.buildModel();
		return launcher;
	}
}