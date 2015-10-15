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
import java.util.List;

public class VarRetrieveAssign extends AbstractProcessor<CtAssignment>  {

	int i=0;
	int j=0;
	
	@Override
	public void process(CtAssignment element) {
		try {
			if(element.getParent(CtStatement.class) instanceof CtLoop) {
				return;
			}
			if(element.getParent(CtReturn.class) instanceof CtLoop) {
				return;
			}
			if(element.getAssignment() == null) {
				return;
			}
		} catch (ParentNotInitializedException e) {
			// ignore the error
		}
		if(element.getAssignment().toString().startsWith(CallChecker.class.getCanonicalName() + ".beforeCalled")) {
			return;
		}
		try{
			j++;
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			execref.setSimpleName("varAssign");
			execref.setStatic(true);
			
			CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			invoc.setExecutable(execref);

			CtExpression assigned = element.getAssigned();
			assigned = removeUnaryOperator(assigned);

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

	private CtExpression removeUnaryOperator(CtExpression assigned) {
		assigned = getFactory().Core().clone(assigned);
		List<CtUnaryOperator> elements = assigned.getElements(new AbstractFilter<CtUnaryOperator>(CtUnaryOperator.class) {
			@Override
			public boolean matches(CtUnaryOperator element) {
				UnaryOperatorKind kind = element.getKind();
				return kind.equals(UnaryOperatorKind.PREDEC) || kind.equals(UnaryOperatorKind.PREINC) ||
						kind.equals(UnaryOperatorKind.POSTDEC) || kind.equals(UnaryOperatorKind.POSTINC);
			}
		});
		if(elements.size() == 0) {
			return assigned;
		}

		for (int k = 0; k < elements.size(); k++) {
			CtUnaryOperator ctUnaryOperator = elements.get(k);
			CtExpression operand = ctUnaryOperator.getOperand();
			UnaryOperatorKind kind = ctUnaryOperator.getKind();

			if (kind.equals(UnaryOperatorKind.POSTDEC)) {
				operand = getFactory().Code().createBinaryOperator(operand, getFactory().Code().createLiteral(1), BinaryOperatorKind.PLUS);
			} else if (kind.equals(UnaryOperatorKind.POSTINC)) {
				operand = getFactory().Code().createBinaryOperator(operand, getFactory().Code().createLiteral(1), BinaryOperatorKind.MINUS);
			}
			operand.setParent(ctUnaryOperator.getParent());
			ctUnaryOperator.replace(operand);
		}

		return assigned;
	}

	@Override
	public void processingDone() {
		System.out.println("assign --> "+j+" (failed: "+i+")");
	}

}
