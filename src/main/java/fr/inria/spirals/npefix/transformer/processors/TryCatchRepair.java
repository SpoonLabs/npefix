package fr.inria.spirals.npefix.transformer.processors;


import fr.inria.spirals.npefix.main.all.TryCatchRepairStrategy;
import fr.inria.spirals.npefix.resi.context.Decision;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;

import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/07/17
 */
@SuppressWarnings("all")
public class TryCatchRepair extends MethodEncapsulation {

	@Override
	public void processingDone() {
		System.out.println("TryCatchRepair # Method: " + MethodEncapsulation.methodNumber + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	protected CtTry createTry(CtLocalVariable methodVar, CtTypeReference tmpref) {
		if ((tmpref instanceof CtTypeParameterReference)) {
			return null;
		}

		CtCatchVariable parameter = getFactory().Code().createCatchVariable(getFactory().Type().createReference(Throwable.class), "_bcornu_return_t");
		parameter.setPosition(methodVar.getPosition());

		CtCatch localCatch = getFactory().Core().createCatch();
		localCatch.setParameter(parameter);
		localCatch.setBody(getFactory().Core().createBlock());

		CtVariableAccess methodAccess = getFactory().createVariableRead();
		methodAccess.setVariable(methodVar.getReference());

		CtReturn ret = getFactory().Core().createReturn();
		ret.setPosition(methodVar.getPosition());

		CtExpression arg = ProcessorUtility.createCtTypeElement(tmpref);

		final CtBinaryOperator equalsLine = getFactory().createBinaryOperator();

		final CtInvocation invocation = getFactory().createInvocation();
		final CtExecutableReference getTargetLine = getFactory().Core().createExecutableReference();
		getTargetLine.setStatic(true);
		getTargetLine.setType(getFactory().Type().integerPrimitiveType());
		getTargetLine.setDeclaringType(getFactory().createCtTypeReference(TryCatchRepairStrategy.class));
		getTargetLine.setSimpleName("getTargetLine");
		invocation.setExecutable(getTargetLine);
		invocation.setTarget(getFactory().createTypeAccess(getFactory().createCtTypeReference(TryCatchRepairStrategy.class)));

		equalsLine.setKind(BinaryOperatorKind.EQ);
		equalsLine.setLeftHandOperand(invocation);
		equalsLine.setRightHandOperand(getFactory().createLiteral(methodVar.getPosition().getLine()));

		final CtIf ctIf = getFactory().createIf();
		ctIf.setCondition(equalsLine);

		final CtThrow aThrow = getFactory().createThrow();
		aThrow.setThrownExpression(getFactory().createVariableRead(parameter.getReference(), false));
		ctIf.setElseStatement(aThrow);

		final CtInvocation<Decision> invokeGetDecision = getFactory().createInvocation();
		final CtExecutableReference<Decision> getDecision = getFactory().createExecutableReference();
		getDecision.setStatic(true);
		getDecision.setType(getFactory().<Decision>createCtTypeReference(Decision.class));
		getDecision.setDeclaringType(getFactory().createCtTypeReference(TryCatchRepairStrategy.class));
		getDecision.setSimpleName("getDecision");

		getDecision.setParameters(Arrays.asList(
				getFactory().createCtTypeReference(Class.class),
				getFactory().Type().STRING,
				getFactory().Type().INTEGER_PRIMITIVE,
				getFactory().Type().INTEGER_PRIMITIVE,
				getFactory().Type().INTEGER_PRIMITIVE
		));

		invokeGetDecision.setArguments(Arrays.asList(
				getFactory().createCodeSnippetExpression("getClass()"),
				getFactory().createCodeSnippetExpression("getClass().toString()"),
				getFactory().Code().createLiteral(methodVar.getPosition().getLine()),
				getFactory().Code().createLiteral(methodVar.getPosition().getSourceStart()),
				getFactory().Code().createLiteral(methodVar.getPosition().getSourceEnd())
		));

		invokeGetDecision.setExecutable(getDecision);
		invokeGetDecision.setTarget(getFactory().createTypeAccess(getFactory().createCtTypeReference(TryCatchRepairStrategy.class)));

		final CtInvocation<Object> invokeGetValue = getFactory().createInvocation();
		final CtExecutableReference<Object> getValue = getFactory().createExecutableReference();
		getValue.setDeclaringType(getFactory().createCtTypeReference(Decision.class));
		getValue.setType(getFactory().createCtTypeReference(Object.class));
		getValue.setSimpleName("getValue");
		invokeGetValue.setExecutable(getValue);
		invokeGetValue.setTarget(invokeGetDecision);

		CtTypeReference variableType = tmpref;
		if(tmpref.equals(getFactory().Type().VOID_PRIMITIVE)){
			variableType = getFactory().Code().createCtTypeReference(Object.class);
			localCatch.getBody().addStatement(invokeGetValue);
		}

		if(!tmpref.equals(getFactory().Type().VOID_PRIMITIVE)){
			invokeGetValue.addTypeCast(variableType.box());
			ret.setReturnedExpression(invokeGetValue);
		}

		ret.setReturnedExpression(invokeGetValue);

		ctIf.setThenStatement(ret);

		localCatch.getBody().addStatement(ctIf);
		localCatch.setPosition(methodVar.getPosition());

		CtExecutableReference executableRef = getFactory().Core().createExecutableReference();
		executableRef.setSimpleName("methodEnd");

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setExecutable(executableRef);
		invoc.setTarget(methodAccess);
		invoc.setPosition(methodVar.getPosition());
		CtBlock finalizer = getFactory().Core().createBlock();
		finalizer.addStatement(invoc);

		CtTry e = getFactory().Core().createTry();
		e.addCatcher(localCatch);
		e.setFinalizer(finalizer);
		e.setPosition(methodVar.getPosition());

		return e;
	}
}
