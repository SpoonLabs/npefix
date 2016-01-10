package fr.inria.spirals.npefix.resi;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import fr.inria.spirals.npefix.resi.Strategy;
import utils.sacha.interfaces.ITestResult;

/**
 * Created by thomas on 13/10/15.
 */
public class NPEDataset extends fr.inria.spirals.npefix.AbstractTest {

    private String rootNPEDataset = "/home/thomas/git/npedataset/";

    private void eval(Map<Strategy, ITestResult>  results) {
        int minFailing = Integer.MAX_VALUE;
        List<Strategy> bestStrats = new ArrayList<>();
        Set<Strategy> strategies = results.keySet();
        for (Iterator<Strategy> iterator = strategies.iterator(); iterator.hasNext(); ) {
            Strategy strategy = iterator.next();
            ITestResult iTestResult = results.get(strategy);
            if(iTestResult.getNbFailedTests() < minFailing) {
                bestStrats = new ArrayList<>();
                bestStrats.add(strategy);
                minFailing = iTestResult.getNbFailedTests();
            } else if(iTestResult.getNbFailedTests() == minFailing) {
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

        Map<Strategy, ITestResult> results = runProject("collections331", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("freemarker02", source, test, deps);
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
        Map<Strategy, ITestResult> results = runProject("jfreechart03", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("lang304", source, test, deps);
        eval(results);
    }

    @Test
    public void lang587() throws Exception {
        String root = rootNPEDataset + "lang-587/";
        String source = root + "src";
        String test = root + "test";
        String[] deps = new String[]{
                "junit/junit/4.7/junit-4.7.jar"
        };

        Map<Strategy, ITestResult> results = runProject("lang587", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("lang703", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math290", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math305", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math369", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math988a", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math988b", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math1115", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("math1117", source, test, deps);
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

        Map<Strategy, ITestResult> results = runProject("mckoi01", source, test, deps);
        eval(results);
    }
}
