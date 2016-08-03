package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.AbstractEvaluation;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by thomas on 13/10/15.
 */
public class NPEDataset extends AbstractEvaluation {

    private static final String rootNPEDataset = "/home/thomas/git/npedataset/";

    private void eval(NPEOutput results) {
        int minFailing = Integer.MAX_VALUE;
        List<Strategy> bestStrats = new ArrayList<>();
        Set<Strategy> runnedStrategies = results.getRanStrategies();
        for (Iterator<Strategy> iterator = runnedStrategies.iterator(); iterator
                .hasNext(); ) {
            Strategy strategy = iterator.next();
            int failureCount = results.getFailureCount(strategy);
            if(failureCount < minFailing) {
                bestStrats = new ArrayList<>();
                bestStrats.add(strategy);
                minFailing = failureCount;
            } else if(failureCount == minFailing) {
                bestStrats.add(strategy);
            }
        }
        String bestStrategies = "";
        for (int i = 0; i < bestStrats.size(); i++) {
            Strategy strategy = bestStrats.get(i);
            if(!bestStrategies.isEmpty()) {
                bestStrategies += ", ";
            }
            bestStrategies += strategy.toString();
        }
        bestStrategies += " with " + minFailing + " failing tests";
        System.out.println(bestStrategies);
        Assert.assertEquals("The best strategies " + bestStrategies, 0, minFailing);
    }

    @Test
    public void collections360() throws Exception {
        // svn 1076034
        String root = rootNPEDataset + "collections-360/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };
        NPEOutput results = runProject("collections360", source, test, deps);
        System.out.println(results);
        boolean start1AOk = results.getExecutionsForStrategy(new Strat1A()).getFailureCount() == 0;
        Assert.assertFalse("Start1A did not work on collections360", start1AOk);

        boolean start1BOk = results.getExecutionsForStrategy(new Strat1B()).getFailureCount() == 0;
        Assert.assertFalse("Start1B did not work on collections360", start1BOk);

        Assert.assertTrue("Start2A empty search space on collections360", results.getExecutionsForStrategy(new Strat2A()).isEmpty());

        Assert.assertTrue("Start2B empty search space on collections360", results.getExecutionsForStrategy(new Strat2B()).isEmpty());

        boolean start3Ok = results.getExecutionsForStrategy(new Strat3()).getFailureCount() == 0;
        Assert.assertFalse("Start3 did not work on collections360", start3Ok);

        Assert.assertTrue("Start4Null empty search space on collections360", results.getExecutionsForStrategy(new Strat4(ReturnType.NULL)).isEmpty());

        Assert.assertTrue("Start4Void empty search space on collections360", results.getExecutionsForStrategy(new Strat4(ReturnType.VOID)).isEmpty());

        boolean start4NewOk = results.getExecutionsForStrategy(new Strat4(ReturnType.NEW)).getFailureCount() == 0;
        Assert.assertTrue("Start4 NEW worked on collections360", start4NewOk);

        boolean start4VarOk = results.getExecutionsForStrategy(new Strat4(ReturnType.VAR)).getFailureCount() == 0;
        Assert.assertTrue("Start4 VAR worked on collections360", start4VarOk);
    }

    @Test
    public void lang304() throws Exception {
        String root = rootNPEDataset + "lang-304/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("lang304", source, test, deps);
        eval(results);
    }

    @Test
    public void lang587() throws Exception {
        String root = rootNPEDataset + "lang-587/";
        String source = root + "src/main";
        String test = root + "src/test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("lang587", source, test, deps);
        eval(results);
    }

    @Test
    public void lang703() throws Exception {
        String root = rootNPEDataset + "lang-703/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("lang703", source, test, deps);
        eval(results);
    }

    @Test
    public void math290() throws Exception {
        String root = rootNPEDataset + "math-290/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math290", source, test, deps);
        eval(results);
    }

    @Test
    public void math305() throws Exception {
        String root = rootNPEDataset + "math-305/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math305", source, test, deps);
        eval(results);
    }

    @Test
    public void math369() throws Exception {
        String root = rootNPEDataset + "math-369/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math369", source, test, deps);
        eval(results);
    }

    @Test
    public void math988a() throws Exception {
        String root = rootNPEDataset + "math-988a/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math988a", source, test, deps);
        eval(results);
    }

    @Test
    public void math988b() throws Exception {
        String root = rootNPEDataset + "math-988b/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math988b", source, test, deps);
        eval(results);
    }

    @Test
    public void math1115() throws Exception {
        String root = rootNPEDataset + "math-1115/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math1115", source, test, deps);
        eval(results);
    }

    @Test
    public void math1117() throws Exception {
        String root = rootNPEDataset + "math-1117/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("math1117", source, test, deps);
        eval(results);
    }

    @Test
    @Ignore
    public void mckoi01() throws Exception {
        String root = rootNPEDataset + "Mckoi-01/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("mckoi01", source, test, deps);
        eval(results);
    }
}
