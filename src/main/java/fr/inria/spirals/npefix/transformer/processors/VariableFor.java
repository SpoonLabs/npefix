package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;

/**
 * Verify that a variable is not null in a loop or a foreach loop if(varA !=null) for(a in varA)
 */
public class VariableFor extends AbstractProcessor<CtVariableRead<?>> {

	@Override
	public boolean isToBeProcessed(CtVariableRead<?> element) {
		CtElement parent = element.getParent();
		if(!(parent instanceof CtForEach
				|| parent instanceof CtFor))
			return false;
		// for variable declared in a for and used oin the declaration ex: for(boolean loop = true;loop;)
		if(element.getVariable().getDeclaration().getParent().equals(element.getParent())) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtVariableRead<?> element) {
		CtElement parent = element.getParent();
		CtExecutableReference execif = getFactory().Core().createExecutableReference();
		execif.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
		execif.setSimpleName("beforeDeref");
		execif.setStatic(true);

		CtInvocationImpl ifInvoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		ifInvoc.setExecutable(execif);
		ifInvoc.setArguments(Arrays.asList(new CtExpression[]{element}));

		CtIf encaps = getFactory().Core().createIf();
		encaps.setCondition(ifInvoc);

		CtBlock thenBloc = getFactory().Core().createBlock();

		((CtStatement) parent).replace(encaps);
		encaps.setThenStatement(thenBloc);
		thenBloc.addStatement((CtStatement) parent);
	}

}
