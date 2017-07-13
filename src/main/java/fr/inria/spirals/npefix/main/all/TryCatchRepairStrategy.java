
package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.Location;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.oracle.TestOracle;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.resi.strategies.Strategy;
import fr.inria.spirals.npefix.transformer.processors.*;
import org.junit.runner.Request;
import org.junit.runner.Result;
import utils.sacha.runner.main.TestRunner;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
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

	@Override
	public NPEOutput run(Selector selector, List<String> methodTests) {
		TryCatchRepairStrategy.selector = selector;
		NPEOutput output = new NPEOutput();
		Lapse lapse = new Lapse(selector);
		final TestRunner testRunner = new TestRunner();
		for (int i = 0; i < methodTests.size(); i++) {
			String method = methodTests.get(i);
			String[] split = method.split("#");
			method = split[1];
			String className = split[0];
			final Request request;
			try {
				request = Request.method(CallChecker.currentClassLoader.loadClass(className), method);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				continue;
			}
			lapse.setTestClassName(className);
			lapse.setTestName(method);
			try {
				selector.startLaps(lapse);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			Result result = testRunner.run(request);

			lapse.setOracle(new TestOracle(result));
			System.out.println(lapse);
			if (result.getRunCount() > 0) {
				output.add(lapse);
			}
			lapse = new Lapse(selector);
		}
		Collections.sort(output);
		return output;
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
