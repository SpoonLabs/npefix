package fr.inria.spirals.npefix.resi.selector;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.RandomGenerator;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import org.junit.Before;

public class DomSelectorEvaluation extends AbstractSelectorEvaluation {

    @Before
    public void setup() {
        RandomGenerator.reset();
        CallChecker.clear();
        setSelector(new DomSelector());
        DomSelector.strategy = new Strat4(ReturnType.VAR);
    }
}
