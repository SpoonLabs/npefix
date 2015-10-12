package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.Strategy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.spirals.npefix.transformer.processors.*;
import spoon.SpoonModelBuilder;
import spoon.processing.ProcessingManager;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.AnnotationFilter;
import spoon.support.QueueProcessingManager;
import utils.TestClassesFinder;
import utils.sacha.interfaces.ITestResult;
import utils.sacha.runner.main.TestRunner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

    private final String[] sourcePath;
    private final String classpath;
    private final String sourceOutput;
    private final String binOutput;
    private spoon.Launcher spoon;

    private final Logger logger = LoggerFactory.getLogger(Launcher.class);

    public Launcher(String[] sourcePath, String sourceOutput, String binOutput, String classpath) {
        this.sourcePath = sourcePath;
        this.classpath = classpath + System.getProperty("java.class.path");
        this.sourceOutput = sourceOutput;
        this.binOutput = binOutput;
    }

    public void instrument() {
        spoon = new spoon.Launcher();
        for (int i = 0; i < sourcePath.length; i++) {
            String s = sourcePath[i];
            spoon.addInputResource(s);
        }

        SpoonModelBuilder compiler = spoon.getModelBuilder();
        compiler.setSourceClasspath(classpath.split(File.pathSeparator));

        spoon.getEnvironment().setCopyResources(true);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(true);
        spoon.getEnvironment().setGenerateJavadoc(false);

        spoon.buildModel();

        ProcessingManager p = new QueueProcessingManager(spoon.getFactory());

        p.addProcessor(IfSplitter.class.getCanonicalName());
        p.addProcessor(ForceNullInit.class.getCanonicalName());
        p.addProcessor(TargetIfAdder.class.getCanonicalName());
        p.addProcessor(TargetModifier.class.getCanonicalName());
        p.addProcessor(TryRegister.class.getCanonicalName());
        p.addProcessor(VarRetrieveAssign.class.getCanonicalName());
        p.addProcessor(VarRetrieveInit.class.getCanonicalName());
        p.addProcessor(MethodEncapsulation.class.getCanonicalName());
        p.addProcessor(VariableFor.class.getCanonicalName());

        spoon.setSourceOutputDirectory(sourceOutput);
        spoon.setBinaryOutputDirectory(binOutput);
        logger.debug("Start code instrumentation");

        ArrayList<CtType<?>> allWithoutTest = new ArrayList<>();
        List<CtType<?>> allClasses = spoon.getFactory().Class().getAll();
        for (int i = 0; i < allClasses.size(); i++) {
            CtType<?> ctType = allClasses.get(i);
            if(ctType.getSimpleName().endsWith("Test")) {
                continue;
            }
            List<CtElement> elements = ctType.getElements(new AnnotationFilter<>(Test.class));
            if(elements.size() > 0) {
                continue;
            }
            allWithoutTest.add(ctType);
        }
        p.process(allWithoutTest);
        spoon.prettyprint();
        compiler.compile();
        logger.debug("End code instrumentation");
    }

    public ITestResult runStrategy(Strategy strategy) {
        CallChecker.strat = strategy;

        ArrayList<URL> uRLClassPath = new ArrayList<>();
        String[] sourceClasspath = spoon.getModelBuilder().getSourceClasspath();

        for (int i = 0; i < sourceClasspath.length; i++) {
            String s = sourceClasspath[i];
            try {
                uRLClassPath.add(new File(s).toURL());
            } catch (MalformedURLException e) {
                continue;
            }
        }
        URLClassLoader urlClassLoader = new URLClassLoader(uRLClassPath.toArray(new URL[]{}));
        String[] testsString = new TestClassesFinder().findIn(urlClassLoader, false);
        testsString = new String[]{"org.junit.tests.AllTests"};
        List<Class> tests = new ArrayList<>();
        for (int i = 0; i < testsString.length; i++) {
            String s = testsString[i];
            if(!isValidTest(s)) {
                continue;
            }
            try {
                Class<?> aClass = urlClassLoader.loadClass(s);
                tests.add(aClass);
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
        return new TestRunner(tests.toArray(new Class[]{})).run();
    }

    private boolean isValidTest(String testName) {
        return spoon.getFactory().Class().get(testName) != null;
    }
}
