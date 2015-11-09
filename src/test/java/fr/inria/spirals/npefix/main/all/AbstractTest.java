package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.*;
import fr.inria.spirals.npefix.resi.strategies.*;
import org.apache.commons.io.FileUtils;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import utils.sacha.interfaces.ITestResult;
import utils.sacha.runner.utils.TestInfo;

import javax.lang.model.element.NestingKind;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by thomas on 13/10/15.
 */
public class AbstractTest {

    private static final String M2REPO = "/home/thomas/.m2/repository/";

    boolean instrumentCode = false;

    protected Map<Strategy, ITestResult>  runProject(String name, String source, String test, String[] deps) {
        return runProject(name, source, test, deps, false);
    }

    protected Map<Strategy, ITestResult>  runProject(String name, String source, String test, String[] deps, boolean printException) {
        return runProject(name, source, test, deps, printException,
                new NoStrat(),
                new Strat1A(),
                new Strat1B(),
                new Strat2A(),
                new Strat2B(),
                new Strat3(),
                new Strat4(ReturnType.NULL),
                new Strat4(ReturnType.VAR),
                new Strat4(ReturnType.NEW),
                new Strat4(ReturnType.VOID));
    }

    protected Map<Strategy, ITestResult> runProject(String name, String source, String test, String[] deps, boolean printException, Strategy... strats) {
        Map<Strategy, ITestResult> results = new HashMap<>();
        Launcher launcher;
        if(instrumentCode) {
            launcher = initNPEFix(name, source, test, deps);
            launcher.instrument();
        } else {
            launcher = initNPEFix(name, "/tmp/npefix/"  + name + "/instrumented", null, deps);
            launcher.getCompiler().compile();
        }

        for (int i = 0; i < strats.length; i++) {
            Strategy strat = strats[i];
            ITestResult iTestResult = runStrategy(launcher, strat);
            results.put(strat, iTestResult);
        }
        printResults(strats, results, printException);
        return results;
    }

    public void printResults(Strategy[] strats,
            Map<Strategy, ITestResult> results,
            boolean printException) {
        Set<String> passedTest = new HashSet<>();
        int totalTest = 0;

        for (int j = 0; j < strats.length; j++) {
            Strategy strategy = strats[j];
            TestInfo iTestResult = (TestInfo) results.get(strategy);
            totalTest = Math.max(totalTest, iTestResult.getNbRunTests());
        }
        int totalFailed = 0;
        if(results.containsKey(new NoStrat())) {
            totalFailed = results.get(new NoStrat()).getNbFailedTests();
            totalFailed += totalTest - results.get(new NoStrat()).getNbRunTests();
        }
        for (int j = 0; j < strats.length; j++) {
            Strategy strategy = strats[j];

            TestInfo iTestResult = (TestInfo) results.get(strategy);
            Result result = iTestResult.getResult();
            List<Failure> failures = result.getFailures();

            Set<String> failingTest = new HashSet<>();
            for (int i = 0; i < failures.size(); i++) {
                Failure failure = failures.get(i);
                failingTest.add(failure.getDescription().getClassName() + "#" + failure.getDescription().getMethodName());
            }

            Set<String> testClasses = iTestResult.keySet();
            for (Iterator<String> stringIterator = testClasses
                    .iterator(); stringIterator.hasNext(); ) {
                String testClass = stringIterator.next();
                List<String> tests = iTestResult.get(testClass);
                for (int i = 0; i < tests.size(); i++) {
                    String test =  tests.get(i);
                    if(!failingTest.contains(testClass + "#" + test)) {
                        passedTest.add(testClass + "#" + test);
                    }
                }
            }

            int countError = 0;
            int countForceReturn = 0;
            int countAbnormalExecution = 0;
            int countFailure = 0;
            int countNPEError = 0;
            int countInitClass = 0;
            int countVarNotFound = 0;
            int countReturnNotSupported = 0;
            result.getIgnoreCount();
            for (int i = 0; i < failures.size(); i++) {
                Failure failure = failures.get(i);
                Throwable exception = failure.getException();
                if(printException) {
                    System.err.println(failure.toString());
                }
                if(exception != null) {
                    if(exception instanceof NullPointerException) {
                        countNPEError++;
                    } else if(exception instanceof fr.inria.spirals.npefix.resi.exception.ForceReturn) {
                        countForceReturn++;
                    } else if(exception instanceof fr.inria.spirals.npefix.resi.exception.ErrorInitClass) {
                        countInitClass++;
                    } else if(exception instanceof fr.inria.spirals.npefix.resi.exception.VarNotFound) {
                        countVarNotFound++;
                    } else if(exception instanceof fr.inria.spirals.npefix.resi.exception.ReturnNotSupported) {
                        countReturnNotSupported++;
                    } else if(exception instanceof fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError) {
                        countAbnormalExecution++;
                    } else if (exception instanceof AssertionError){
                        countFailure ++;
                    } else {
                        countError++;
                    }
                    if(printException) {
                        printException(exception);
                        System.err.println("\n\n");
                    }
                }
            }
            int nbFailed = iTestResult.getNbFailedTests();
            nbFailed += totalTest - iTestResult.getNbRunTests();
            String output = String.format(
                    "%s -> %d/%d %d fixed (Error %d, ForceReturn %d, AbnormalExecution %d, Return not supported %d, Error init class %d, var not found %d, NPEError %d, Failure %d) (%d ms)",
                    strategy.toString(),
                    nbFailed,
                    totalTest,
                    totalFailed - nbFailed,
                    countError,
                    countForceReturn,
                    countAbnormalExecution,
                    countReturnNotSupported,
                    countInitClass,
                    countVarNotFound,
                    countNPEError,
                    countFailure,
                    result.getRunTime());

            System.out.println(output);
        }

        String output = String.format("The %d strategies pass %d tests",
                strats.length,
                passedTest.size());
        System.out.println(output);
    }

    protected Launcher initNPEFix(String name, String source, String test, String[] deps) {
        File binFolder = new File("/tmp/npefix/" + name + "/bin");
        File outputSource = new File("/tmp/npefix/"  + name + "/instrumented");

        binFolder.mkdirs();
        outputSource.mkdirs();
        Launcher launcher = new Launcher(
                new String[]{source, test},
                outputSource.getAbsolutePath(),
                binFolder.getAbsolutePath(),
                binFolder.getAbsolutePath() + File.pathSeparator + depArrayToClassPath(deps));

        return launcher;
    }

    private void cleanOutput(File binFolder, File outputSource) {
        try {
            if(binFolder.exists()) {
                FileUtils.deleteDirectory(binFolder);
            }
            if(outputSource.exists()) {
                FileUtils.deleteDirectory(outputSource);
            }
        } catch (IOException e) {
            // the file must be present
        }
    }

    protected ITestResult runStrategy(Launcher launcher, Strategy strategy) {
        System.out.println("Start " + strategy);
        ITestResult results = launcher.runStrategy(strategy);
        System.out.println("End  " + strategy + " " + results.getResult().getRunTime() + "ms");
        return results;
    }

    private void printException(Throwable exception) {
        System.err.println(exception.getClass() + ": " + exception.getMessage());
        for (int i = 0; i < exception.getStackTrace().length && i < 25; i++) {
            StackTraceElement trace = exception.getStackTrace()[i];
            System.err.println("    at " + trace.getClassName() + '.' + trace.getMethodName() + '(' + trace.getFileName() + ':' + trace.getLineNumber() + ')');
        }
        if(exception.getCause() != null) {
            System.err.println("Caused By:");
            printException(exception.getCause());
        }
    }

    protected String depArrayToClassPath(String[] deps) {
        String classpath = "";
        for (int i = 0; i < deps.length; i++) {
            String dep = deps[i];
            classpath += M2REPO + dep + File.pathSeparator;
        }
        return classpath;
    }
}
