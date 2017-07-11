package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.LineFilter;

import java.util.Date;

public class VarRetrieveAssign extends AbstractProcessor<CtAssignment>  {

	private Date start;
	int nbError = 0;
	int nbAssignment = 0;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public boolean isToBeProcessed(CtAssignment element) {
		if(element.getAssignment() == null) {
			return false;
		}
		if (element.getAssignment().toString().contains(CallChecker.class.getSimpleName() + ".beforeCalled")) {
			return false;
		}
		if(element.getParent(CtLambda.class) != null) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtAssignment element) {
		try{
			nbAssignment++;

			CtExpression assigned = element.getAssigned();
			/*if(element.getAssignment().getElements(new AbstractFilter<CtInvocation>(CtInvocation.class) {}).size() == 0) {
				assigned = element.getAssignment();
			}*/
			assigned = ProcessorUtility.removeUnaryOperator(assigned, false);

			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

			CtLiteral<String> variableName = getFactory().Code().createLiteral(assigned.toString());

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
				CtStatement parent;
				if (element.getParent() instanceof CtStatementList) {
					parent = element;
				} else {
					parent = element.getParent(new LineFilter());
				}
				if (parent == null) {
					return;
				}
				if(parent instanceof CtReturn) {
					return;
				}
				if (parent instanceof CtLoop) {
					CtStatement body = ((CtLoop) parent).getBody();
					if (!(body instanceof CtBlock)) {
						((CtLoop) parent).setBody(getFactory().Code().createCtBlock(body));
					}
					((CtBlock)body).insertBegin(invoc);
				} else {
					parent.insertAfter(invoc);
				}
				invoc.setParent(parent);
			}

		}catch(Throwable t){
			nbError++;
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
		System.out.println("Assign --> " + nbAssignment + " (failed: "+ nbError +")" + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

}
