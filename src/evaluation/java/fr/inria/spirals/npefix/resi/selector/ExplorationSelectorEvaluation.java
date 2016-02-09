package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.RandomGenerator;
import org.junit.Before;

public class ExplorationSelectorEvaluation extends AbstractSelectorEvaluation {

    public ExplorationSelectorEvaluation () {
        nbIteration = 1000000;
        Config.CONFIG.setNbIteration(nbIteration);
    }

    @Before
    public void setup() {
        RandomGenerator.reset();
        CallChecker.clear();
        setSelector(new ExplorerSelector());
    }
}
