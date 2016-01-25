package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.RandomGenerator;
import org.junit.Before;

/**
 * Created by thomas on 13/10/15.
 */
public class GreedySelectorEvaluation extends AbstractSelectorEvaluation {

    @Before
    public void setup() {
        RandomGenerator.reset();
        CallChecker.clear();
        setSelector(new GreedySelector());
    }
}
