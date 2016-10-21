package fr.inria.spirals.npefix.patchTemplate;

import fr.inria.spirals.npefix.patchTemplate.template.ReplaceGlobal;
import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtForEach;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class ReplaceGlobalTest {

	@Test
	public void ReplaceGlobalInstanceTest() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get("Foo");

		CtForEach afor = (CtForEach) foo.getMethod("foo2").getBody().getStatement(1);
		Assert.assertEquals("for (String element : array) {\n"
				+ "    result += element.toString();\n"
				+ "    if (element == null) {\n"
				+ "        return null;\n"
				+ "    }\n"
				+ "}", afor.toString());

		new ReplaceGlobal(foo.getFactory().Class().get(ArrayList.class)).apply(afor.getExpression());

		Assert.assertEquals("if ((array) == null)\n"
				+ "    this.array = new ArrayList();\n",
				foo.getMethod("foo2").getBody().getStatement(1).toString());
	}

	@Test
	public void ReplaceGlobalVariableTest() {
		Launcher spoon = getSpoonLauncher();

		CtClass foo = spoon.getFactory().Class().get("Foo");

		CtForEach afor = (CtForEach) foo.getMethod("foo2").getBody().getStatement(1);
		Assert.assertEquals("for (String element : array) {\n"
				+ "    result += element.toString();\n"
				+ "    if (element == null) {\n"
				+ "        return null;\n"
				+ "    }\n"
				+ "}", afor.toString());

		new ReplaceGlobal(foo.getField("field")).apply(afor.getExpression());

		Assert.assertEquals("if ((array) == null)\n"
				+ "    this.array = this.field;\n",
				foo.getMethod("foo2").getBody().getStatement(1).toString());
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