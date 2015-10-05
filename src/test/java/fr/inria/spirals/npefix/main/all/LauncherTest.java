package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.strategies.*;
import org.junit.Assert;
import org.junit.Test;
import utils.sacha.interfaces.ITestResult;

import java.net.URL;


public class LauncherTest {

    @Test
    public void testInstrument() throws Exception {
        URL sourcePath = getClass().getResource("/foo/src/main/java/");
        URL testPath = getClass().getResource("/foo/src/test/java/");
        URL rootPath = getClass().getResource("/foo/");
        String classpath = System.getProperty("java.class.path");
        Launcher launcher = new Launcher(new String[]{sourcePath.getFile(),testPath.getFile()},
                rootPath.getFile() + "/target/instrumented",
                rootPath.getFile() + "/target/classes",
                classpath);
        launcher.instrument();
    }

    @Test
    public void testRun() throws Exception {
        URL sourcePath = getClass().getResource("/foo/src/main/java/");
        URL testPath = getClass().getResource("/foo/src/test/java/");
        URL rootPath = getClass().getResource("/");
        String classpath = System.getProperty("java.class.path");

        Launcher launcher = new Launcher(
                new String[]{sourcePath.getFile(),
                        testPath.getFile()},
                rootPath.getFile() + "/../instrumented",
                rootPath.getFile() + "",
                classpath);
        launcher.instrument();
        ITestResult results = launcher.runStrategy(new NoStrat());
        Assert.assertEquals("NoStrat total", 1, results.getNbRunTests());
        Assert.assertEquals("NoStrat failing", 1, results.getNbFailedTests());

        results = launcher.runStrategy(new Strat1A());
        Assert.assertEquals("Strat1A total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat1A failing", 1,results.getNbFailedTests());


        results = launcher.runStrategy(new Strat1B());
        Assert.assertEquals("Strat1B total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat1B failing", 1,results.getNbFailedTests());


        results = launcher.runStrategy(new Strat2A());
        Assert.assertEquals("Strat2A total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat2A failing", 0,results.getNbFailedTests());


        results = launcher.runStrategy(new Strat2B());
        Assert.assertEquals("Strat2B total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat2B failing", 0,results.getNbFailedTests());


        results = launcher.runStrategy(new Strat3());
        Assert.assertEquals("Strat3 total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat3 failing", 0,results.getNbFailedTests());


        results = launcher.runStrategy(new Strat4(ReturnType.NULL));
        Assert.assertEquals("Strat4 Null total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat4 Null failing", 0,results.getNbFailedTests());


        results = launcher.runStrategy(new Strat4(ReturnType.VAR));
        Assert.assertEquals("Strat4 var total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat4 var failing", 0,results.getNbFailedTests());

        results = launcher.runStrategy(new Strat4(ReturnType.NEW));
        Assert.assertEquals("Strat4 new total", 1, results.getNbRunTests());
        Assert.assertEquals("Strat4 new failing", 0,results.getNbFailedTests());

    }
}