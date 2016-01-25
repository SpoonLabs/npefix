package fr.inria.spirals.npefix.resi;

import fr.inria.spirals.npefix.AbstractEvaluation;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
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

    private String rootNPEDataset = "/home/thomas/git/npedataset/";

    private void eval(NPEOutput results) {
        int minFailing = Integer.MAX_VALUE;
        List<Strategy> bestStrats = new ArrayList<>();
        Set<Strategy> runnedStrategies = results.getRunnedStrategies();
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
    public void collections331() throws Exception {
        String root = rootNPEDataset + "collections-331/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("collections331", source,
                test, deps);
        eval(results);
    }

    @Test
    @Ignore
    public void freemarker02() throws Exception {
        String root = rootNPEDataset + "freemarker-02/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        NPEOutput results = runProject("freemarker02", source, test, deps);
        eval(results);
    }

    @Test
    @Ignore
    public void jfreechart03() throws Exception {
        String root = rootNPEDataset + "jfreechart-03/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar",
                "org/jfree/jcommon/1.0.23/jcommon-1.0.23.jar",
                "javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar"
        };
        NPEOutput results = runProject("jfreechart03", source, test, deps);
        eval(results);
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
