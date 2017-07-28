
package fr.inria.spirals.npefix.main.all;

import fr.inria.spirals.npefix.resi.context.NPEOutput;
import fr.inria.spirals.npefix.resi.selector.Selector;
import fr.inria.spirals.npefix.transformer.processors.AddImplicitCastChecker;
import fr.inria.spirals.npefix.transformer.processors.CheckNotNull;
import fr.inria.spirals.npefix.transformer.processors.ConstructorTryCatchRepair;
import fr.inria.spirals.npefix.transformer.processors.ForceNullInit;
import fr.inria.spirals.npefix.transformer.processors.TernarySplitter;
import fr.inria.spirals.npefix.transformer.processors.TryCatchRepair;
import fr.inria.spirals.npefix.transformer.processors.VarRetrieveAssign;
import fr.inria.spirals.npefix.transformer.processors.VarRetrieveInit;
import fr.inria.spirals.npefix.transformer.processors.VariableFor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/07/17
 */
@SuppressWarnings("all")
public class TryCatchRepairStrategy extends DefaultRepairStrategy {
	private static Selector selector;

	public TryCatchRepairStrategy() {
		processors = new ArrayList<>();
		processors.add(new TernarySplitter());//
		processors.add(new CheckNotNull());//
		processors.add(new ForceNullInit());//
		processors.add(new AddImplicitCastChecker());//
		processors.add(new VarRetrieveAssign());//
		processors.add(new VarRetrieveInit());//
		processors.add(new TryCatchRepair());
		processors.add(new ConstructorTryCatchRepair());
		processors.add(new VariableFor());//
	}

	public NPEOutput run(Selector selector, List<String> methodTests) {
		TryCatchRepairStrategy.selector = selector;
		return super.run(selector, methodTests);
	}
}
