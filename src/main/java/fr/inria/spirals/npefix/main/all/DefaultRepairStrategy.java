package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Lapse;
import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.exception.NoMoreDecision;
import fr.inria.spirals.npefix.resi.oracle.TestOracle;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.transformer.processors.AddImplicitCastChecker;
import fr.inria.spirals.npefix.transformer.processors.BeforeDerefAdder;
import fr.inria.spirals.npefix.transformer.processors.CheckNotNull;
import fr.inria.spirals.npefix.transformer.processors.ConstructorEncapsulation;
import fr.inria.spirals.npefix.transformer.processors.ForceNullInit;
import fr.inria.spirals.npefix.transformer.processors.MethodEncapsulation;
import fr.inria.spirals.npefix.transformer.processors.TargetModifier;
import fr.inria.spirals.npefix.transformer.processors.TernarySplitter;
import fr.inria.spirals.npefix.transformer.processors.TryRegister;
import fr.inria.spirals.npefix.transformer.processors.VarRetrieveAssign;
import fr.inria.spirals.npefix.transformer.processors.VarRetrieveInit;
import fr.inria.spirals.npefix.transformer.processors.VariableFor;
import org.junit.runner.Request;
import org.junit.runner.Result;
import spoon.processing.AbstractProcessor;
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
public class DefaultRepairStrategy implements RepairStrategy {

	protected List<AbstractProcessor> processors;

	public DefaultRepairStrategy() {
		processors = new ArrayList<>();
		processors.add(new TernarySplitter());//
		//processors.add(new IfSplitter());
		processors.add(new CheckNotNull());//
		processors.add(new ForceNullInit());//
		processors.add(new AddImplicitCastChecker());//
		processors.add(new BeforeDerefAdder());
		processors.add(new TargetModifier());
		processors.add(new TryRegister());
		processors.add(new VarRetrieveAssign());//
		processors.add(new VarRetrieveInit());//
		processors.add(new MethodEncapsulation());
		processors.add(new ConstructorEncapsulation());
		processors.add(new VariableFor());//
		//p.addProcessor(new ArrayRead());
	}

	@Override
	public NPEOutput run(Selector selector, List<String> methodTests) {
		CallChecker.enable();
		CallChecker.strategySelector = selector;

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

			TestOracle oracle = new TestOracle(result);
			lapse.setOracle(oracle);
			if (result.getRunCount() > 0) {
				if (oracle.isValid() || !oracle.getError().contains(NoMoreDecision.class.getSimpleName())) {
					System.out.println(lapse);
				}
				output.add(lapse);
				try {
					selector.restartTest(lapse);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}
			lapse = new Lapse(selector);
			CallChecker.enable();
			CallChecker.cache.clear();
			CallChecker.getDecisions().clear();
		}
		Collections.sort(output);
		return output;
	}

	@Override
	public List<AbstractProcessor> getListOfProcessors() {
		return this.processors;
	}

}
