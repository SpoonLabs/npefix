package fr.inria.spirals.npefix.resi.selector;

import org.junit.Before;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.RandomGenerator;

public class SafeMonoEvaluation extends AbstractSelectorEvaluation {
    
    @Before
    public void setup() {
        RandomGenerator.reset();
        CallChecker.clear();
        Config.CONFIG.setMultiPoints(false);
        setSelector(new SafeMonoSelector());
    }
}
