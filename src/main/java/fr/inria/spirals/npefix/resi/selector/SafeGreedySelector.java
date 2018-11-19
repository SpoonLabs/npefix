package fr.inria.spirals.npefix.resi.selector;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.inria.spirals.npefix.config.Config;
import fr.inria.spirals.npefix.resi.RandomGenerator;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.strategies.ReturnType;
import fr.inria.spirals.npefix.resi.strategies.NoStrat;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import fr.inria.spirals.npefix.resi.strategies.Strategy;

import static fr.inria.spirals.npefix.resi.context.Decision.DecisionType.BEST;
import static fr.inria.spirals.npefix.resi.context.Decision.DecisionType.NEW;
import static fr.inria.spirals.npefix.resi.context.Decision.DecisionType.RANDOM;

/**
 * A selector for a safe runnning mode for the NPEFix
 * @author benjamin
 *
 */

public class SafeGreedySelector extends AbstractSelector {
    private double epsilon;
    private Map<Decision<?>, Integer> counts = new HashMap<>();
    private Map<Decision<?>, Double> values = new HashMap<>();
    private Set<Decision<?>> usedDecisions = new HashSet<>();
    private List<Decision<?>> unusedDecisions  = new ArrayList<>();
    
    public SafeGreedySelector() {
        epsilon = Config.CONFIG.getGreedyEpsilon();
    }
    
    @Override
    public boolean startLaps(Lapse lapse) throws RemoteException {
        super.startLaps(lapse);
        return true;
    }
    
    private <T> void initDecision(List<Decision<T>> decisions) {
        for (int i = 0; i < decisions.size(); i++) {
            Decision decision = decisions.get(i);
            if(!counts.containsKey(decision)) {
                counts.put(decision, 0);
                values.put(decision, 0.0);
                unusedDecisions.add(decision);
            }
        }
    }
    
    @Override
    public Set<Decision> getSearchSpace() {
        HashSet<Decision> decisions = new HashSet<>();
        decisions.addAll(unusedDecisions);
        decisions.addAll(usedDecisions);
        return decisions;
    }
    
    @Override
    public <T> Decision<T> select(List<Decision<T>> decisions) throws RemoteException {
        initDecision(decisions);

		Collections.shuffle(decisions, RandomGenerator.getGenerator());

		double random = RandomGenerator.nextDouble();

		ArrayList<Decision<T>> localUnusedDecision = new ArrayList<>();
		for (int i = 0; i < decisions.size(); i++) {
			Decision<T> tDecision = decisions.get(i);
			if(!usedDecisions.contains(tDecision)) {
				localUnusedDecision.add(tDecision);
			}
		}

		// strat with max values
		if(random  > this.epsilon && localUnusedDecision.size() != decisions.size()) {
			double maxValue = -1;
			Decision bestDecision = null;
			for (int i = 0; i < decisions.size(); i++) {
				Decision decision = decisions.get(i);
				double value = values.get(decision);
				if(value > maxValue) {
					bestDecision = decision;
					maxValue = value;
				}
			}
			usedDecisions.add(bestDecision);
			bestDecision.setDecisionType(BEST);
			getCurrentLapse().putMetadata("strategy_selection", "best");
			bestDecision.setEpsilon(epsilon);
			return bestDecision;
		}
		// return a random strategy
		getCurrentLapse().putMetadata("strategy_selection", "random");
		Decision output;
		if(localUnusedDecision.isEmpty()) {
			int maxValue = decisions.size();
			int index = RandomGenerator.nextInt(1, maxValue);
			output = decisions.get(index);
			output.setDecisionType(RANDOM);
		} else {
			output = localUnusedDecision.remove(0);
			usedDecisions.add(output);
			output.setDecisionType(NEW);
		}
		output.setEpsilon(epsilon);
		return output;

    }

    /*
     * TODO
     * Should probably just return a shorter list of strategies 
     * rather than removing as many as we return
     */
    @Override
    public List<Strategy> getStrategies() throws RemoteException {
        ArrayList<Strategy> strategies = new ArrayList<>(getAllStrategies());
        strategies.remove(new NoStrat());
        strategies.remove(new Strat1A());
        strategies.remove(new Strat1B());
        strategies.remove(new Strat2A());
        strategies.remove(new Strat2B());
        return strategies;
    }
    
    @Override
    public boolean restartTest(Lapse lapse) {
        super.restartTest(lapse);
        if(lapse.getDecisions().isEmpty()) {
            return false;
            //return !laps.getOracle().wasSuccessful();
        }

        for (int i = 0; i < lapse.getDecisions().size(); i++) {
            Decision decision =  lapse.getDecisions().get(i);

            int count = counts.get(decision) + 1;
            counts.put(decision, count);
            double value = values.get(decision);
            int reward = 0;
            if(lapse.getOracle() != null &&
                    lapse.getOracle().isValid()) {
                reward = 1;
            }

            double newValue = Math.min(1, ((count - 1) / (float) count) * value + (1 / (float) count) * reward);
            //System.out.println(decision.toString() + " " + newValue + " " + reward);
            values.put(decision, newValue);
        }
        return false;
        //return !laps.getOracle().wasSuccessful();
    }
}
