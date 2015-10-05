package utils.sacha.impl;

import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.finder.main.TestClassFinder;

import java.io.PrintStream;

public class TestSuiteCreatorCore extends AbstractConfigurator{

	public void printJavaTestSuite(PrintStream out, String className){
		EnrichableClassloader eClassloader = getEnrichableClassloader();
		
		Thread.currentThread().setContextClassLoader(eClassloader);
		
		Class<?>[] tests = new TestClassFinder(eClassloader).findTestClasses();

		if(tests.length==0)
			throw new IllegalArgumentException("no tests found in "+getProjectDir().getAbsolutePath());
		
		String classes = "";
		for (Class<?> clazz : tests) {
			classes+=clazz.getName()+".class";
			classes+=",";
		}
		classes = classes.substring(0, classes.length()-1);
		
		out.print("\n" +
				"import org.junit.internal.TextListener;\n"+
				"import org.junit.runner.JUnitCore;\n"+
				"import org.junit.runner.Result;\n" +
				"\n" +
				"public class "+className+"{\n" +
					"\tpublic static void main(String[] args){\n" +
						"\t\tnew "+className+"().run();\n"+
					"\t}\n" +
					"\n" +
					"\tpublic Result run(){\n" +
						"\t\tJUnitCore runner = new JUnitCore();\n"+
						"\t\trunner.addListener(new TextListener(System.out));\n"+
						"\t\treturn runner.run(getClassesArray());\n"+
					"\t}\n" +
					"\n"+
					"\tprivate Class<?>[] getClassesArray(){\n" +
						"\t\treturn new Class<?>[]{"+classes+"};\n"+
					"\t}\n" +
				"}");
	}
}
