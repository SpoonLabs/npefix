
package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import fr.inria.spirals.npefix.transformer.processors.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/07/17
 */
@SuppressWarnings("all")
public class TryCatchRepairStrategy extends DefaultRepairStrategy {

	public static int targetLine;

	public static int getTargetLine() {
		return targetLine;
	}

	private static Selector selector;

	public TryCatchRepairStrategy(int targetLine) {
		TryCatchRepairStrategy.targetLine = targetLine;
		processors = new ArrayList<>();
		processors.add(new TernarySplitter());//
		processors.add(new CheckNotNull());//
		processors.add(new ForceNullInit());//
		processors.add(new AddImplicitCastChecker());//
		processors.add(new VarRetrieveAssign());//
		processors.add(new VarRetrieveInit());//
		processors.add(new TryCatchRepair());
//		processors.add(new ConstructorEncapsulation()); //TODO
		processors.add(new VariableFor());//
	}

	public NPEOutput run(Selector selector, List<String> methodTests) {
		TryCatchRepairStrategy.selector = selector;
		return super.run(selector, methodTests);
	}

	public static Decision<Object> getDecision(Class clazz, String className, int line, int sourceStart, int sourceEnd) {
		try {
			final Strategy strategy = selector.getStrategies().get(0);
			final Location location = new Location(className, line, sourceStart, sourceEnd);
			final List<Decision<Object>> searchSpace = strategy.getSearchSpace(null, clazz, location);//TODO
			final Decision<Object> decision = selector.select(searchSpace);
			selector.getCurrentLapse().addApplication(decision);
			return decision;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

}
