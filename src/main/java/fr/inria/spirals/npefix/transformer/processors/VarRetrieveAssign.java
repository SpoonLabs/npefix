package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.support.reflect.code.CtArrayWriteImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtVariableReadImpl;

import java.util.Arrays;

public class VarRetrieveAssign extends AbstractProcessor<CtAssignment>  {

	int i=0;
	int j=0;
	
	@Override
	public void process(CtAssignment element) {
		if(element.getParent() instanceof CtLoop) {
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
			CtLiteral<String> variableName =null;
			if(element.getAssigned() instanceof CtVariableAccess) {
				variableName = getFactory().Code().createLiteral(((CtVariableAccess) element.getAssigned()).getVariable().getSimpleName());
			} else if(element.getAssigned() instanceof CtArrayAccess) {
				CtExpression target = (((CtArrayAccess) element.getAssigned()).getTarget());
				CtVariableAccess variable = null;
				if(target.toString().startsWith(CallChecker.class.getSimpleName())) {
					while(target instanceof CtTargetedExpression && ((CtTargetedExpression) target).getTarget() != null) {
						target = ((CtTargetedExpression) target).getTarget();
					}
					variable =((CtVariableAccess)((CtInvocation) target).getArguments().get(0));
				} else if(target instanceof CtVariableAccess) {
					variable = (CtVariableAccess) target;
				}
				variableName = getFactory().Code().createLiteral(variable.getVariable().getSimpleName());
			}
			CtExpression assigned = element.getAssigned();
			if(assigned instanceof CtArrayAccess) {
				CtExpression indexExpression = ((CtArrayAccess) assigned).getIndexExpression();
				if(indexExpression instanceof CtUnaryOperator) {
					UnaryOperatorKind kind = ((CtUnaryOperator) indexExpression).getKind();
					assigned = getFactory().Core().clone(assigned);
					CtExpression operand = ((CtUnaryOperator) indexExpression).getOperand();
					if (kind.equals(UnaryOperatorKind.PREDEC) || kind.equals(UnaryOperatorKind.PREINC)) {
						((CtArrayAccess) assigned).setIndexExpression(operand);
					} else if (kind.equals(UnaryOperatorKind.POSTDEC)) {
						CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(operand, getFactory().Code().createLiteral(1), BinaryOperatorKind.PLUS);
						((CtArrayAccess) assigned).setIndexExpression(binaryOperator);
					} else if (kind.equals(UnaryOperatorKind.POSTINC)) {
						CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(operand, getFactory().Code().createLiteral(1), BinaryOperatorKind.MINUS);
						((CtArrayAccess) assigned).setIndexExpression(binaryOperator);
					}


				}
			}
			invoc.setArguments(Arrays.asList(new Object[]{variableName, assigned}));

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
				((CtStatement)parent).insertAfter(invoc);
				invoc.setParent(parent);
			}

		}catch(Throwable t){
			i++;
		}
	}
	
	@Override
	public void processingDone() {
		System.out.println("assign --> "+j+" (failed: "+i+")");
	}

}
