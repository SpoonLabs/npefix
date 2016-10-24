package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.RandomGenerator;
import org.junit.Before;

public class MonoExplorationEvaluation extends AbstractSelectorEvaluation {

    @Before
    public void setup() {
        RandomGenerator.reset();
        CallChecker.clear();
        Config.CONFIG.setMultiPoints(false);
        setSelector(new MonoExplorerSelector());
    }
}
