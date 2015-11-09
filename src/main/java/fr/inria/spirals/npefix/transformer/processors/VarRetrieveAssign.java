package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;

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
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			execref.setSimpleName("varAssign");
			execref.setStatic(true);
			
			CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			invoc.setExecutable(execref);

			CtExpression assigned = element.getAssigned();
			/*if(element.getAssignment().getElements(new AbstractFilter<CtInvocation>(CtInvocation.class) {}).size() == 0) {
				assigned = element.getAssignment();
			}*/
			assigned = ProcessorUtility.removeUnaryOperator(assigned, false);

			invoc.setArguments(Arrays.asList(new Object[]{assigned}));

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
