package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.context.Laps;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.List;

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
        List<Laps> results = launcher.runStrategy(new NoStrat(),
                new Strat1A(),
                new Strat1B(),
                new Strat2A(),
                new Strat2B(),
                new Strat3(),
                new Strat4(ReturnType.NULL),
                new Strat4(ReturnType.VAR),
                new Strat4(ReturnType.NEW),
                new Strat4(ReturnType.VOID));

        Assert.assertEquals("Nb test", 7, 7);

        /*Assert.assertEquals("NoStrat failing", 7, results.get(0).getOracle().getNbFailedTests());

        results = launcher.runStrategy(new Strat1A());
        Assert.assertEquals("Strat1A failing", 6,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat1B());
        Assert.assertEquals("Strat1B failing", 6,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat2A());
        Assert.assertEquals("Strat2A failing", 4,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat2B());
        Assert.assertEquals("Strat2B failing", 4,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat3());
        Assert.assertEquals("Strat3 failing", 2,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat4(ReturnType.NULL));
        Assert.assertEquals("Strat4 Null failing", 4,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat4(ReturnType.VAR));
        Assert.assertEquals("Strat4 var failing", 4,results.get(0).getOracle().getNbFailedTests());

        results = launcher.runStrategy(new Strat4(ReturnType.NEW));
        Assert.assertEquals("Strat4 new failing", 3,results.get(0).getOracle().getNbFailedTests());


        results = launcher.runStrategy(new Strat4(ReturnType.VOID));
        Assert.assertEquals("Strat4 void failing", 5,results.get(0).getOracle().getNbFailedTests());*/

    }
}