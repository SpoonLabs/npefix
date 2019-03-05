package fr.inria.spirals.npefix.resi.selector;

import java.util.ArrayList;
import java.util.List;

import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

/**
 * An implementation of MonoExplorerSelector that should only utilise strategies
 * 3 and 4.
 * @author benjamin
 *
 */

public class SafeMonoSelector extends MonoExplorerSelector{
    
    @Override
    public List<Strategy> getStrategies() {
        ArrayList<Strategy> strategies = new ArrayList<>();
        strategies.add(new Strat3());
        strategies.add(new Strat4(ReturnType.NULL));
        strategies.add(new Strat4(ReturnType.VAR));
        strategies.add(new Strat4(ReturnType.NEW));
        strategies.add(new Strat4(ReturnType.VOID));
        return strategies;
    }
}
