package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;

/**
 * Encapsulate array access in our framework
 */
public class ArrayRead extends spoon.processing.AbstractProcessor<CtArrayRead> {
	@Override
	public boolean isToBeProcessed(CtArrayRead candidate) {
		if (candidate.getParent() instanceof CtAssignment) {
			if (((CtAssignment) candidate.getParent()).getAssigned() == candidate) {
				return false;
			}
		}
		return super.isToBeProcessed(candidate);
	}

	@Override
	public void process(CtArrayRead e) {
		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(e.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(e.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(e.getPosition().getSourceEnd());

		CtInvocation arrayAccess = ProcessorUtility.createStaticCall(getFactory(),
				CallChecker.class,
				"arrayAccess",
				e.getTarget().clone(),
				e.getIndexExpression(),
				ProcessorUtility.createCtTypeElement(e.getType()),
				lineNumber,
				sourceStart,
				sourceEnd);
		e.replace(arrayAccess);
	}
}
