package fr.inria.spirals.npefix;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.main.DecisionServer;
import fr.inria.spirals.npefix.main.all.Launcher;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

public class AbstractEvaluation {

    private static final String M2REPO = Config.CONFIG.getM2Repository();

    boolean instrumentCode = true;

    protected NPEOutput runProject(String name, String source, String test, String[] deps) {
        return runProject(name, source, test, deps, false);
    }

    protected NPEOutput runProject(String name, String source, String test, String[] deps, boolean printException) {
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

    protected NPEOutput multipleRunsProject(String name, String source,
            String test, String[] deps, boolean printException, int nbIteration, Selector selector) {
        NPEOutput output = new NPEOutput();
        Launcher launcher;
        if(instrumentCode) {
            launcher = initNPEFix(name, source, test, deps);
            launcher.instrument();
        } else {
            launcher = initNPEFix(name, Config.CONFIG.getEvaluationWorkingDirectory() + "/" + name + "/instrumented", null, deps);
            //launcher.getCompiler().compile();
        }
        DecisionServer decisionServer = new DecisionServer(selector);
        decisionServer.startServer();
        List<String> tests = launcher.getTests();
        if(test.isEmpty()) {
            throw new RuntimeException("No test found");
        }
        Date initEndDate = new Date();
        int countError = 0;
        while (output.size() < nbIteration) {
            if(countError > 5) {
                break;
            }
            try {
                List<Lapse> result = launcher.run(selector, tests);
                if(result.isEmpty()) {
                    countError++;
                    continue;
                }
                boolean isEnd = true;
                for (int i = 0; i < result.size() && isEnd; i++) {
                    Lapse lapse = result.get(i);
                    if (lapse.getOracle().getError() != null) {
                        isEnd = isEnd && lapse.getOracle().getError().contains("No more available decision");
                    } else {
                        isEnd = false;
                    }
                }
                if (isEnd) {
                    // no more decision
                    countError++;
                    continue;
                }
                countError = 0;
                if(output.size() + result.size() > nbIteration) {
                    output.addAll(result.subList(0, (nbIteration - output.size())));
                } else {
                    output.addAll(result);
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                countError++;
                continue;
            } catch (Exception e) {
                if(e.getCause() instanceof OutOfMemoryError) {
                    countError++;
                    continue;
                }
                e.printStackTrace();
                countError++;
                continue;
            }
            System.out.println("Multirun " + output.size() + "/" + nbIteration + " " + ((int)(output.size()/(double)nbIteration * 100)) + "%");
        }
        output.setEnd(new Date());

        spoon.Launcher spoon = new spoon.Launcher();
        spoon.addInputResource(source);
        spoon.getModelBuilder().setSourceClasspath(launcher.getSpoon().getModelBuilder().getSourceClasspath());
        spoon.buildModel();

        JSONObject jsonObject = output.toJSON(spoon);
        jsonObject.put("endInit", initEndDate.getTime());
        try {
            for (Decision decision : selector.getSearchSpace()) {
                jsonObject.append("searchSpace", decision.toJSON());
            }
            serializeResult(jsonObject, name, selector.toString());
            printResults(output, printException);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } finally {
            decisionServer.stopServer();
        }
        return output;
    }

    public void serializeResult(JSONObject results, String project, String selector) {
        try {
            File file = new File(Config.CONFIG.getOutputDirectory() + selector + "/" + project + "/" + (new Date().getTime()) + ".json");
            if(!file.exists()) {
                FileUtils.forceMkdir(file.getParentFile());
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file);
            results.write(fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected NPEOutput runProject(String name, String source, String test, String[] deps, boolean printException, Strategy... strats) {
        Launcher launcher;
        if(instrumentCode) {
            launcher = initNPEFix(name, source, test, deps);
            launcher.instrument();
        } else {
            launcher = initNPEFix(name, Config.CONFIG.getEvaluationWorkingDirectory()  + name + "/instrumented", null, deps);
            launcher.getCompiler().compile();
        }

        spoon.Launcher spoon = new spoon.Launcher();
        spoon.addInputResource(source);
        spoon.getModelBuilder().setSourceClasspath(launcher.getSpoon().getModelBuilder().getSourceClasspath());
        spoon.buildModel();

        NPEOutput results = runStrategy(launcher, strats);
        serializeResult(results.toJSON(spoon), name, "DomSelector");
        printResults(results, printException);
        return results;
    }

    public void printResults(NPEOutput results,
            boolean printException) {
        /*List<Decision> strats = new ArrayList<>(results.getRanStrategies());

        Set<String> passedTest = new HashSet<>();

        int totalTest = results.getTests().size();
        int totalFailed = 0;
        if(!results.getExecutionsForStrategy(new NoStrat()).isEmpty()) {
            totalFailed = results.getFailureCount(new NoStrat());
        }
        for (Iterator<Decision> iterator = strats.iterator(); iterator
                .hasNext(); ) {
            Decision strategy = iterator.next();
            NPEOutput execStrat = results.getExecutionsForStrategy(strategy);

            Set<String> failingTest = new HashSet<>();
            List<Failure> failures = new ArrayList<>();
            for (int i = 0; i < execStrat.size(); i++) {
                Laps execution = execStrat.get(i);
                List<Failure> fails = execution.getOracle().getFailures();
                failures.addAll(fails);
                for (int j = 0; j < fails.size(); j++) {
                    Failure failure = fails.get(j);
                    failingTest.add(failure.getDescription().getClassName() + "#" + failure.getDescription().getMethodName());
                }

                /*Set<String> testClasses = iTestResult.keySet();
                for (Iterator<String> stringIterator = testClasses
                        .iterator(); stringIterator.hasNext(); ) {
                    String testClass = stringIterator.next();
                    List<String> tests = iTestResult.get(testClass);
                    for (int j = 0; j < tests.size(); j++) {
                        String test =  tests.get(j);
                        if(!failingTest.contains(testClass + "#" + test)) {
                            passedTest.add(testClass + "#" + test);
                        }
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

            for (int j = 0; j < failures.size(); j++) {
                Failure failure = failures.get(j);
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
            int nbFailed = execStrat.getFailureCount(strategy);
            String output = String.format(
                    "%s -> %d/%d %d fixed (Error %d, ForceReturn %d, AbnormalExecution %d, Return not supported %d, Error initClass class %d, var not found %d, NPEError %d, Failure %d)",
                    strategy,
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
                    countFailure);

            System.out.println(output);
        }

        System.out.println("Test \t Strategy \t Location \t # application \t # values");

        for (int i = 0; i < results.size(); i++) {
            Laps execution = results.get(i);
            Method test = execution.getTest();
            String testClassName = test.getDeclaringClass().getCanonicalName();
            String testName = test.getName();

            for (Iterator<Strategy> iterator = strats.iterator(); iterator
                    .hasNext(); ) {
                Strategy strategy = iterator.next();
                if(execution.getMainDecision() == null
                    || !execution.getMainDecision().equals(strategy)) {
                    continue;
                }
                int countApplication = 0;

                Map<Location, Integer> appliStrat = execution.getNbApplication();

                List<Location> locations = new ArrayList<>(appliStrat.keySet());
                Collections.sort(locations);

                for (int j = 0; j < locations.size(); j++) {
                    Location location =  locations.get(j);
                    countApplication += appliStrat.get(location);
                }

                for (int j = 0; j < locations.size(); j++) {
                    Location location =  locations.get(j);

                    if(appliStrat.containsKey(location)) {
                        String status = "Ok";

                        Result result = execution.getOracle();
                        List<Failure> failures = result.getFailures();
                        for (int k = 0; k < failures.size(); k++) {
                            Failure failure = failures.get(k);
                            if(failure == null
                                    || failure.getDescription() == null
                                    || failure.getDescription().getMethodName() == null
                                    || failure.getDescription().getClassName() == null) {
                                continue;
                            }
                            if(failure.getDescription().getMethodName().equals(testName) &&
                                    failure.getDescription().getClassName().equals(testClassName)) {
                                status = "Ko";
                                break;
                            }
                        }
                        String variableContent = "";
                        if(execution.getPossibleValues() != null &&
                                execution.getPossibleValues().containsKey(location) &&
                                execution.getPossibleValues().get(location).size() > execution.getCurrentIndex().get(location)) {
                            variableContent += execution.getPossibleValues().get(location).get(execution.getCurrentIndex().get(location)) + "";
                        } else if (execution.getPossibleVariables() != null &&
                                execution.getPossibleVariables().containsKey(location) &&
                                execution.getPossibleVariables().get(location).size() > execution.getCurrentIndex().get(location)) {
                            variableContent += execution.getPossibleVariables().get(location).values().toArray()[execution.getCurrentIndex().get(location)] + "";
                        }
                        String format = String.format("%s \t %s \t %d \t %s \t %d \t %s \t %s",
                                testClassName.replace("org.apache.commons.", "") + "#" + testName,
                                strategy,
                                countApplication,
                                location,
                                appliStrat.get(location),
                                "V" + (execution.getCurrentIndex().get(location) + 1) + " [" + variableContent + "]",
                                status);
                        System.out.println(format);
                    }
                }
            }
        }

        String output = String.format("The %d strategies pass %d tests",
                strats.size(),
                passedTest.size());
        System.out.println(output);*/
    }

    protected Launcher initNPEFix(String name, String source, String test, String[] deps) {
        File binFolder = new File(Config.CONFIG.getEvaluationWorkingDirectory() + "/" + name + "/bin");
        File outputSource = new File(Config.CONFIG.getEvaluationWorkingDirectory()  + "/" + name + "/instrumented");

        binFolder.mkdirs();
        outputSource.mkdirs();

        Launcher launcher = new Launcher(
                new String[]{source, test},
                outputSource.getAbsolutePath(),
                binFolder.getAbsolutePath(),
                binFolder.getAbsolutePath() + File.pathSeparator + depArrayToClassPath(deps));

        return launcher;
    }

    protected NPEOutput runStrategy(Launcher launcher, Strategy...strategy) {
        NPEOutput results = launcher.runStrategy(strategy);
        return results;

    }

    protected static String depArrayToClassPath(String...deps) {
        String classpath = "";
        for (int i = 0; i < deps.length; i++) {
            String dep = deps[i];
            classpath += M2REPO + dep + File.pathSeparator;
        }
        return classpath;
    }

    public boolean isInstrumentCode() {
        return instrumentCode;
    }
}
