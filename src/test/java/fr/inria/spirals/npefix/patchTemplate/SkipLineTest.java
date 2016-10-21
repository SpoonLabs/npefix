package fr.inria.spirals.npefix.patchTemplate;

import fr.inria.spirals.npefix.patchTemplate.template.SkipLine;
import org.junit.Assert;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonModelBuilder;
import spoon.reflect.code.CtForEach;
import spoon.reflect.declaration.CtClass;

import java.io.File;
import java.net.URL;

public class SkipLineTest {

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

		new SkipLine().apply(afor.getExpression());

		Assert.assertEquals("if ((array) != null)\n"
				+ "    for (java.lang.String element : array) {\n"
				+ "        result += element.toString();\n"
				+ "        if (element == null) {\n"
				+ "            return null;\n"
				+ "        }\n"
				+ "    }\n", foo.getMethod("foo2").getBody().getStatement(1).toString());
	}


	private spoon.Launcher getSpoonLauncher() {
		URL sourcePath = getClass().getResource("/foo/src/main/java/");
		URL testPath = getClass().getResource("/foo/src/test/java/");
		String classpath = System.getProperty("java.class.path");

		spoon.Launcher launcher = new spoon.Launcher();
		launcher.addInputResource(sourcePath.getPath());
		launcher.addInputResource(testPath.getPath());
		SpoonModelBuilder compiler = launcher.getModelBuilder();
		compiler.setSourceClasspath(classpath.split(File.pathSeparator));
		launcher.buildModel();
		return launcher;
	}
}