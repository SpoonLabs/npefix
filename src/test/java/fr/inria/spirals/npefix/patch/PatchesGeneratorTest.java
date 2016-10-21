package fr.inria.spirals.npefix.patch;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.instance.NewArrayInstance;
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
				new Location("Foo", 21, 392, 396));
		foo.setValueType(Object.class);
		foo.setValue(new NewInstance(Object.class.getCanonicalName(), new String[0], Collections.emptyList()));
		foo.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -20,2 +20,5 @@\n"
				+ "         String result = \"\";\n"
				+ "+        if (array == null) {\n"
				+ "+            return new Object();\n"
				+ "+        }\n"
				+ "         for (String element : array) {\n", s);
	}

	@Test
	public void testGeneratePatchStrat4Var() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat4(ReturnType.VAR), new Location("Foo", 21, 392, 396));
		foo.setValueType(Object.class);
		foo.setValue(new VariableInstance("result"));
		foo.setUsed(true);

		Decision field = new Decision(new Strat3(), new Location("Foo", 31, 616, 620));
		field.setValueType(Object.class);
		field.setValue(new VariableInstance("element"));
		field.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);
		lapse.addDecision(field);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -20,2 +20,5 @@\n"
				+ "         String result = \"\";\n"
				+ "+        if (array == null) {\n"
				+ "+            return result;\n"
				+ "+        }\n"
				+ "         for (String element : array) {\n"
				+ "@@ -30,3 +33,5 @@\n"
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
				new Location("Foo", 21, 392, 396));
		foo.setValueType(Object.class);
		foo.setValue(new VariableInstance("result"));
		foo.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -20,6 +20,8 @@\n"
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
				new Location("Foo", 21, 392, 396));
		foo.setValueType(String[].class);
		foo.setValue(new NewArrayInstance(String[].class, Collections.EMPTY_LIST));
		foo.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -20,2 +20,5 @@\n"
				+ "         String result = \"\";\n"
				+ "+        if (array == null) {\n"
				+ "+            array = new String[0];\n"
				+ "+        }\n"
				+ "         for (String element : array) {\n", s);
	}


	@Test
	public void testGeneratePatchStrat1A() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat1A(),
				new Location("Foo", 21, 392, 396));
		foo.setValueType(Object.class);
		foo.setValue(new VariableInstance("result"));
		foo.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -20,7 +20,16 @@\n"
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
				new Location("Foo", 21, 392, 396));
		foo.setValueType(Object.class);
		foo.setValue(new NewArrayInstance(String[].class, Collections.EMPTY_LIST));
		foo.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -20,7 +20,16 @@\n"
				+ "         String result = \"\";\n"
				+ "-        for (String element : array) {\n"
				+ "-            result += element.toString();\n"
				+ "-            if(element == null) {\n"
				+ "-                return null;\n"
				+ "+        if (array == null) {\n"
				+ "+            for (String element : new String[0]) {\n"
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
	public void testMutliPatch() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat2A(), new Location("Foo", 106, 2258, 2262));
		foo.setValueType(String.class);
		foo.setValue(new NewInstance(String.class.getCanonicalName(), new String[0], Collections.EMPTY_LIST));
		foo.setUsed(true);

		Decision field = new Decision(new Strat2A(), new Location("Foo", 106, 2276, 2280));
		field.setValueType(String.class);
		field.setValue(new NewInstance(String.class.getCanonicalName(), new String[0], Collections.EMPTY_LIST));
		field.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);
		lapse.addDecision(field);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -105,3 +105,7 @@\n"
				+ "     public void  multiDecisionLine() {\n"
				+ "-        Arrays.asList(field.toString(), field.toString());\n"
				+ "+        if (field == null) {\n"
				+ "+            Arrays.asList( new String().toString(), new String().toString());\n"
				+ "+        } else {\n"
				+ "+            Arrays.asList(field.toString(), field.toString());\n"
				+ "+        }\n"
				+ "     }\n", s);
	}

	@Test
	public void testElseIfPatch() throws Exception {
		spoon.Launcher launcher = getSpoonLauncher();

		Decision foo = new Decision(new Strat2A(), new Location("Foo", 112, 2383, 2390));
		foo.setValueType(String.class);
		foo.setValue(new NewInstance(String.class.getCanonicalName(), new String[0], Collections.EMPTY_LIST));
		foo.setUsed(true);

		Lapse lapse = new Lapse(new DomSelector());
		lapse.addDecision(foo);

		String s = lapse.toDiff(launcher);
		System.out.println(s);
		Assert.assertEquals(""
				+ "--- main/java/Foo.java\n"
				+ "+++ main/java/Foo.java\n"
				+ "@@ -111,4 +111,12 @@\n"
				+ " \n"
				+ "-        } else if (array[0].isEmpty()) {\n"
				+ "-\n"
				+ "+        } else {\n"
				+ "+            if (array[0] == null) {\n"
				+ "+                if ( new String().isEmpty()) {\n"
				+ "+                    \n"
				+ "+                }\n"
				+ "+            } else {\n"
				+ "+                if (array[0].isEmpty()) {\n"
				+ "+                    \n"
				+ "+                }\n"
				+ "+            }\n"
				+ "         }\n", s);
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