package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtLocalVariableImpl;

import java.util.Date;

/**
 * Created by thomas on 12/01/16.
 */
public class AddImplicitCastChecker extends AbstractProcessor<CtTypedElement>{

	private Date start;
	private int nbImplicitCast;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("AddImplicitCastChecker --> " + nbImplicitCast + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtTypedElement candidate) {
		CtElement parent = candidate.getParent();
		if(!(parent instanceof CtReturn
				|| parent instanceof CtLocalVariable
				|| parent instanceof CtAssignment)) {
			return false;
		}
		if (candidate instanceof CtLiteral) {
			return false;
		}
		CtTypeReference type = candidate.getType();
		if(type == null) {
			System.out.println(parent);
			return false;
		}
		try {
			if(!type.unbox().isPrimitive()) {
				return false;
			}
		} catch (Throwable e) {
			return false;
		}

		if(type.isPrimitive()) {
			return false;
		}
		if(parent instanceof CtReturn) {
			CtMethod ctMethod = candidate.getParent(CtMethod.class);
			return ctMethod.getType().isPrimitive();
		}
		if(parent instanceof CtTypedElement) {
			CtTypeReference type1 = ((CtTypedElement) parent).getType();
			if (type1 == null) {
				return false;
			}
			return type1.isPrimitive();
		}
		return super.isToBeProcessed(candidate);
	}

	@Override
	public void process(CtTypedElement element) {
		CtStatement parent = (CtStatement) element.getParent();
		CtTypeReference parentType = null;
		if(parent instanceof CtReturn) {
			CtMethod ctMethod = element.getParent(CtMethod.class);
			parentType = ctMethod.getType();
		} else if(parent instanceof CtTypedElement) {
			parentType = ((CtTypedElement)parent).getType();
		} else {
			return;
		}
		nbImplicitCast++;

		CtStatement output = (CtStatement) parent;
		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		CtExpression target = (CtExpression) getFactory().Core().clone(element);
		target.addTypeCast(element.getType());

		CtInvocation invocation = ProcessorUtility
				.createStaticCall(getFactory(),
						CallChecker.class,
						"isCalled",
						target,
						ProcessorUtility.createCtTypeElement(parentType),
						lineNumber,
						sourceStart,
						sourceEnd);
		target.setParent(invocation);
		invocation.setPosition(element.getPosition());
		invocation.setType(parentType);

		((CtExpression)element).replace(invocation);

		if(parent instanceof CtLocalVariable) {
			if(((CtLocalVariable) parent).hasModifier(ModifierKind.FINAL)){
				((CtLocalVariable) parent).removeModifier(ModifierKind.FINAL);
			}
			CtInvocation initInvoc = ProcessorUtility.createStaticCall(getFactory(), CallChecker.class, "init", ProcessorUtility.createCtTypeElement(parentType));
			((CtLocalVariable) parent).setDefaultExpression(initInvoc);
			CtAssignment variableAssignment = getFactory().Code()
					.createVariableAssignment(
							((CtLocalVariableImpl) parent).getReference(),
							false, invocation);
			variableAssignment.setPosition(element.getPosition());
			parent.insertAfter(variableAssignment);
			element = variableAssignment.getAssignment();
		}
		if(element.getParent() instanceof CtAssignment) {
			CtInvocation beforeDerefInvocation = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"beforeDeref",
					target,
					ProcessorUtility.createCtTypeElement(parentType),
					lineNumber,
					sourceStart,
					sourceEnd);

			final CtIf encaps = getFactory().Core().createIf();
			encaps.setPosition(element.getPosition());
			encaps.setCondition(beforeDerefInvocation);
			CtBlock thenBloc = getFactory().Core().createBlock();
			thenBloc.setPosition(element.getPosition());
			CtStatement parentInvocation = (CtStatement) invocation.getParent();
			thenBloc.addStatement(getFactory().Core().clone(parentInvocation));

			encaps.setThenStatement(thenBloc);
			parentInvocation.replace(encaps);
		}
	}
}
