package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.AbstractEvaluation;
import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import org.junit.Ignore;
import org.junit.Test;
import utils.TestClassesFinder;
import utils.sacha.runner.main.TestRunner;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Ignore
public class BenchmarkPaper extends AbstractEvaluation {

    private static final int NBITERATION = 10;

    private double runBench(String name, String source, String test, String[] deps) {
        Launcher launcher = initNPEFix(name, source, test, deps);
        //launcher.instrument();
        long start = System.currentTimeMillis();
        for (int i = 0; i < NBITERATION; i++) {
            runStrategy(launcher, new NoStrat());
        }
        long end = System.currentTimeMillis();
        return (end - start)/(double)NBITERATION;
    }

    private double runBench(String[] deps, String binClass, String binTest) {
        List<String> dependencies = new ArrayList<>();
        dependencies.add(binClass);
        if(binTest != null) {
            dependencies.add(binTest);
        }
        dependencies.addAll(Arrays.asList(deps));
        URLClassLoader urlClassLoader = Launcher.getUrlClassLoader(dependencies.toArray(new String[dependencies.size()]));

        String[] testsString = new TestClassesFinder().findIn(urlClassLoader, false);
        Class[] tests = filterTest(urlClassLoader, testsString);
        long start = System.currentTimeMillis();
        for (int i = 0; i < NBITERATION; i++) {
            new TestRunner().run(tests);
        }
        long end = System.currentTimeMillis();
        return (end - start)/(double)NBITERATION;
    }

    private Class[] filterTest(URLClassLoader urlClassLoader, String[] testsString) {
        List<Class> tests = new ArrayList<>();
        for (int i = 0; i < testsString.length; i++) {
            String s = testsString[i];
            try {
                Class<?> aClass = urlClassLoader.loadClass(s);
                tests.add(aClass);
            } catch (ClassNotFoundException e) {
                continue;
            }
        }
        return tests.toArray(new Class[]{});
    }

    @Test
    public void spojo() throws Exception {
        String root = "/home/thomas/git/Spojo/spojo-core/";
        String source = root + "src/main/java";
        String test = root + "src/test/java";

        String binSource = root + "target/classes/";
        String binTest = root + "target/test-classes/";
        String[] deps = new String[]{
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "junit/junit/4.7/junit-4.7.jar",
                "org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar",
                "org/slf4j/slf4j-log4j12/1.6.1/slf4j-log4j12-1.6.1.jar",
                "log4j/log4j/1.2.16/log4j-1.2.16.jar",
                "org/springframework/spring-beans/3.0.6.RELEASE/spring-beans-3.0.6.RELEASE.jar",
                "org/springframework/spring-core/3.0.6.RELEASE/spring-core-3.0.6.RELEASE.jar",
                "org/springframework/spring-asm/3.0.6.RELEASE/spring-asm-3.0.6.RELEASE.jar",
                "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar",
                "com/google/code/gson/gson/1.4/gson-1.4.jar"
        };


        double executionTime = runBench(deps, binSource, binTest);
        double executionTimeTransformed = runBench("spojo", source, test, deps);

        System.out.println("Execution time normal " + executionTime);
        System.out.println("Execution time transformed " + executionTimeTransformed);
        System.out.println("Overhead " + (int)((executionTimeTransformed-executionTime)/executionTime * 100)  + "%");
    }

    @Test
    public void commonsCodec() throws Exception {
        String root = "/home/thomas/git/commons-codec/";
        String source = root + "src/main/java";
        String test = root + "src/test/java";

        String binSource = root + "target/classes/";
        String binTest = root + "target/test-classes/";

        String[] deps = new String[]{
                "junit/junit/4.12/junit-4.12.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar",
                "org/apache/commons/commons-lang3/3.4/commons-lang3-3.4.jar"
        };

        double executionTime = runBench(deps, binSource, binTest);
        double executionTimeTransformed = runBench("commonsCodec", source, test, deps);

        System.out.println("Execution time normal " + executionTime);
        System.out.println("Execution time transformed " + executionTimeTransformed);
        System.out.println("Overhead " + (int)((executionTimeTransformed-executionTime)/executionTime * 100)  + "%");
    }

    @Test
    public void commonsOkio() throws Exception {
        String root = "/home/thomas/git/okio/okio/";
        String source = root + "src/main/java";
        String test = root + "src/test/java";

        String binSource = root + "target/classes/";
        String binTest = root + "target/test-classes/";
        String[] deps = new String[]{
                "junit/junit/4.12/junit-4.12.jar",
                "org/codehaus/mojo/animal-sniffer-annotations/1.10/animal-sniffer-annotations-1.10.jar",
                "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
        };

        double executionTime = runBench(deps, binSource, binTest);
        double executionTimeTransformed = runBench("commonsOkio", source, test, deps);

        System.out.println("Execution time normal " + executionTime);
        System.out.println("Execution time transformed " + executionTimeTransformed);
        System.out.println("Overhead " + (int)((executionTimeTransformed-executionTime)/executionTime * 100)  + "%");
    }
}
