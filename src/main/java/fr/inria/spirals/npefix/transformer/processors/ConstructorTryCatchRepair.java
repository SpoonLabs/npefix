package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.reference.CtExecutableReference;

import java.util.Date;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/07/17
 */
@SuppressWarnings("all")
public class ConstructorTryCatchRepair extends ConstructorEncapsulation {

	@Override
	public void processingDone() {
		System.out.println("ConstructorTryCatchRepair # Constructor: " + contructor + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	protected CtTry createTry(CtLocalVariable methodVar) {

		CtCatchVariable parameter = getFactory().Code().createCatchVariable(getFactory().Type().createReference(RuntimeException.class), "_bcornu_return_t");
		parameter.setPosition(methodVar.getPosition());

		CtCatch localCatch = getFactory().Core().createCatch();
		localCatch.setParameter(parameter);
		localCatch.setBody(getFactory().Core().createBlock());

		CtVariableAccess methodAccess = getFactory().createVariableRead();
		methodAccess.setVariable(methodVar.getReference());

		CtExpression typeMethod = ProcessorUtility.createCtTypeElement(getFactory().Type().voidPrimitiveType());

		final CtInvocation invocation =  ProcessorUtility.createStaticCall(getFactory(),
				CallChecker.class,
				"isToCatch",
				getFactory().createVariableRead().setVariable(parameter.getReference()),
				typeMethod
				);
		localCatch.getBody().addStatement(invocation);

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
