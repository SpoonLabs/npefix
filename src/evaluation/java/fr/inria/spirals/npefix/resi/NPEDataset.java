package fr.inria.spirals.npefix.resi;

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by thomas on 13/10/15.
 */
public class NPEDataset extends AbstractNPEDataset {
    @Override
    public void eval(NPEOutput results) {
        int minFailing = Integer.MAX_VALUE;
        List<Strategy> strategies = new ArrayList<>();
        Set<Strategy> ranStrategies = results.getRanStrategies();
        for (Iterator<Strategy> iterator = ranStrategies.iterator(); iterator
                .hasNext(); ) {
            Strategy strategy = iterator.next();
            int failureCount = results.getFailureCount(strategy);
            if(failureCount < minFailing) {
                strategies = new ArrayList<>();
                strategies.add(strategy);
                minFailing = failureCount;
            } else if(failureCount == minFailing) {
                strategies.add(strategy);
            }
        }
        String bestStrategies = "";
        for (int i = 0; i < strategies.size(); i++) {
            Strategy strategy = strategies.get(i);
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
        NPEOutput results = runProject(COLLECTIONS_360, source, test, deps);
        System.out.println(results);
        boolean start1AOk = results.getExecutionsForStrategy(new Strat1A()).getFailureCount() == 0;
        Assert.assertFalse("Start1A did not work on collections360", start1AOk);

        boolean start1BOk = results.getExecutionsForStrategy(new Strat1B()).getFailureCount() == 0;
        Assert.assertFalse("Start1B did not work on collections360", start1BOk);

        NPEOutput executionsForStrategy = results.getExecutionsForStrategy(new Strat2A());
        System.out.println(executionsForStrategy.size());
        //Assert.assertTrue("Start2A empty search space on collections360", executionsForStrategy.isEmpty());

        NPEOutput executionsForStrategy1 = results.getExecutionsForStrategy(new Strat2B());
        System.out.println(executionsForStrategy1.size());
        //Assert.assertTrue("Start2B empty search space on collections360", executionsForStrategy1.isEmpty());

        boolean start3Ok = results.getExecutionsForStrategy(new Strat3()).getFailureCount() == 0;
        Assert.assertFalse("Start3 did not work on collections360", start3Ok);

        Assert.assertTrue("Start4Null empty search space on collections360", results.getExecutionsForStrategy(new Strat4(ReturnType.NULL)).isEmpty());

        Assert.assertTrue("Start4Void empty search space on collections360", results.getExecutionsForStrategy(new Strat4(
                ReturnType.VOID)).isEmpty());

        boolean start4NewOk = results.getExecutionsForStrategy(new Strat4(ReturnType.NEW)).getFailureCount() == 0;
        Assert.assertTrue("Start4 NEW worked on collections360", start4NewOk);

        boolean start4VarOk = results.getExecutionsForStrategy(new Strat4(ReturnType.VAR)).getFailureCount() == 0;
        Assert.assertTrue("Start4 VAR worked on collections360", start4VarOk);
    }
}
