package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ParentNotInitializedException;

public class VarRetrieveAssign extends AbstractProcessor<CtAssignment>  {

	int i=0;
	int j=0;

	@Override
	public boolean isToBeProcessed(CtAssignment element) {
		try {
			if(element.getParent(CtStatement.class) instanceof CtLoop) {
				return false;
			}
			if(element.getParent(CtReturn.class) instanceof CtLoop) {
				return false;
			}
			if(element.getAssignment() == null) {
				return false;
			}
		} catch (ParentNotInitializedException e) {
			// ignore the error
		}
		if(element.getAssignment().toString().contains(CallChecker.class.getSimpleName() + ".beforeCalled")) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtAssignment element) {
		try{
			j++;

			CtExpression assigned = element.getAssigned();
			/*if(element.getAssignment().getElements(new AbstractFilter<CtInvocation>(CtInvocation.class) {}).size() == 0) {
				assigned = element.getAssignment();
			}*/
			assigned = ProcessorUtility.removeUnaryOperator(assigned, false);

			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

			CtLiteral<String> variableName = getFactory().Code()
					.createLiteral(assigned.toString());

			CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"varAssign",
					assigned,
					variableName,
					lineNumber,
					sourceStart,
					sourceEnd);
			invoc.setPosition(element.getPosition());


			if (element.getParent() instanceof CtStatementList) {
				element.insertAfter(invoc);
				invoc.setParent(element.getParent());
			} else {
				if(element.getParent(CtStatementList.class) == null) {
					return;
				}
				CtElement parent = element.getParent();
				while (!(parent.getParent() instanceof CtStatementList)) {
					parent = parent.getParent();
				}
				if(parent instanceof CtReturn) {
					return;
				}
				((CtStatement)parent).insertAfter(invoc);
				invoc.setParent(parent);
			}

		}catch(Throwable t){
			i++;
			t.printStackTrace();
		}
	}

	private CtExpression getTargetInINPECheck(CtExpression target) {
		if(target.toString().startsWith(CallChecker.class.getSimpleName()) ||
				target.toString().startsWith(CallChecker.class.getCanonicalName())) {
			while (target instanceof CtTargetedExpression &&
					((CtTargetedExpression) target).getTarget() != null) {
				target = ((CtTargetedExpression) target).getTarget();
			}
			CtElement ctElement = (CtElement) ((CtInvocation) target).getArguments().get(0);
			if(ctElement instanceof CtArrayAccess) {
				target = getTargetInINPECheck(((CtArrayAccess) ctElement).getTarget());
			}
		}
		return target;
	}

	@Override
	public void processingDone() {
		System.out.println("assign --> "+j+" (failed: "+i+")");
	}

}
