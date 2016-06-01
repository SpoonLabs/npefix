package fr.inria.spirals.npefix.patch;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Laps;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.instance.ArrayInstance;
import fr.inria.spirals.npefix.resi.context.instance.NewInstance;
import fr.inria.spirals.npefix.resi.context.instance.VariableInstance;
import fr.inria.spirals.npefix.resi.selector.DomSelector;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import org.junit.Assert;
import org.junit.Test;
import spoon.SpoonModelBuilder;

import java.io.File;
import java.net.URL;
import java.util.Collections;


public class PatchesGeneratorTest {
	@Test
	public void testGeneratePatchStrat4New() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat4(ReturnType.NEW),
				new Location("Foo", 20, 367, 371));
		foo.setValueType(Object.class);
		foo.setValue(new NewInstance(Object.class.getCanonicalName(), new String[0], Collections
				.emptyList()));

		Laps laps = new Laps(new DomSelector());
		laps.addDecision(foo);

		String s = laps.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -19,2 +19,5 @@\n"
				+ "         String result = \"\";\n"
				+ "+        if (array == null) {\n"
				+ "+            return new java.lang.Object();\n"
				+ "+        }\n"
				+ "         for (String element : array) {\n", s);
	}

	@Test
	public void testGeneratePatchStrat4Var() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat4(ReturnType.VAR), new Location("Foo", 20, 367, 371));
		foo.setValueType(Object.class);
		foo.setValue(new VariableInstance("result"));

		Decision field = new Decision(new Strat3(), new Location("Foo", 30, 591, 595));
		field.setValueType(Object.class);
		field.setValue(new VariableInstance("element"));

		Laps laps = new Laps(new DomSelector());
		laps.addDecision(foo);
		laps.addDecision(field);

		String s = laps.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -19,2 +19,5 @@\n"
				+ "         String result = \"\";\n"
				+ "+        if (array == null) {\n"
				+ "+            return result;\n"
				+ "+        }\n"
				+ "         for (String element : array) {\n"
				+ "@@ -29,3 +32,5 @@\n"
				+ "     public String fooLocal() {\n"
				+ "-        System.out.print(field.toLowerCase());\n"
				+ "+        if (field != null) {\n"
				+ "+            System.out.print(field.toLowerCase());\n"
				+ "+        }\n"
				+ "         if(field == null) {\n", s);
	}

	@Test
	public void testGeneratePatchStrat3() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat3(),
				new Location("Foo", 20, 367, 371));
		foo.setValueType(Object.class);
		foo.setValue(new VariableInstance("result"));

		Laps laps = new Laps(new DomSelector());
		laps.addDecision(foo);

		String s = laps.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -19,6 +19,8 @@\n"
				+ "         String result = \"\";\n"
				+ "-        for (String element : array) {\n"
				+ "-            result += element.toString();\n"
				+ "-            if(element == null) {\n"
				+ "-                return null;\n"
				+ "+        if (array != null) {\n"
				+ "+            for (String element : array) {\n"
				+ "+                result += element.toString();\n"
				+ "+                if(element == null) {\n"
				+ "+                    return null;\n"
				+ "+                }\n"
				+ "             }\n", s);
	}

	@Test
	public void testGeneratePatchStrat2B() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat2B(),
				new Location("Foo", 20, 367, 371));
		foo.setValueType(String[].class);
		foo.setValue(new ArrayInstance(String[].class, Collections.EMPTY_LIST));

		Laps laps = new Laps(new DomSelector());
		laps.addDecision(foo);

		String s = laps.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -19,2 +19,5 @@\n"
				+ "         String result = \"\";\n"
				+ "+        if (array == null) {\n"
				+ "+            array = new java.lang.String[]{};\n"
				+ "+        }\n"
				+ "         for (String element : array) {\n", s);
	}


	@Test
	public void testGeneratePatchStrat1A() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat1A(),
				new Location("Foo", 20, 367, 371));
		foo.setValueType(Object.class);
		foo.setValue(new VariableInstance("result"));

		Laps laps = new Laps(new DomSelector());
		laps.addDecision(foo);

		String s = laps.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -19,7 +19,16 @@\n"
				+ "         String result = \"\";\n"
				+ "-        for (String element : array) {\n"
				+ "-            result += element.toString();\n"
				+ "-            if(element == null) {\n"
				+ "-                return null;\n"
				+ "+        if (array == null) {\n"
				+ "+            for (String element : result) {\n"
				+ "+                result += element.toString();\n"
				+ "+                if(element == null) {\n"
				+ "+                    return null;\n"
				+ "+                }\n"
				+ "             }\n"
				+ "+        } else {\n"
				+ "+            for (String element : array) {\n"
				+ "+                result += element.toString();\n"
				+ "+                if(element == null) {\n"
				+ "+                    return null;\n"
				+ "+                }\n"
				+ "+            }\n"
				+ "         }\n", s);
	}

	@Test
	public void testGeneratePatchStrat2A() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat2A(),
				new Location("Foo", 20, 367, 371));
		foo.setValueType(Object.class);
		foo.setValue(new ArrayInstance(String[].class, Collections.EMPTY_LIST));

		Laps laps = new Laps(new DomSelector());
		laps.addDecision(foo);

		String s = laps.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -19,7 +19,16 @@\n"
				+ "         String result = \"\";\n"
				+ "-        for (String element : array) {\n"
				+ "-            result += element.toString();\n"
				+ "-            if(element == null) {\n"
				+ "-                return null;\n"
				+ "+        if (array == null) {\n"
				+ "+            for (String element : new java.lang.String[]{}) {\n"
				+ "+                result += element.toString();\n"
				+ "+                if(element == null) {\n"
				+ "+                    return null;\n"
				+ "+                }\n"
				+ "             }\n"
				+ "+        } else {\n"
				+ "+            for (String element : array) {\n"
				+ "+                result += element.toString();\n"
				+ "+                if(element == null) {\n"
				+ "+                    return null;\n"
				+ "+                }\n"
				+ "+            }\n"
				+ "         }\n", s);
	}

	public spoon.Launcher getSpoonLauncher() {
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