package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;
import java.util.List;

public class VarRetrieveInit extends AbstractProcessor<CtLocalVariable>  {

	@Override
	public boolean isToBeProcessed(CtLocalVariable element) {
		if(element.getParent() instanceof CtForEach
				|| element.getParent() instanceof CtFor)
			return false;
		if(element.getType().getPackage()!=null && element.getType()
				.getPackage()
				.toString()
				.startsWith("fr.inria.spirals.npefix")) {
			return false;
		}
		if (element.getSimpleName().startsWith("npe_")) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtLocalVariable element) {
		CtExecutableReference execref = getFactory().Core().createExecutableReference();
		execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
		execref.setSimpleName("varInit");
		execref.setStatic(true);
		
		CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		invoc.setExecutable(execref);
		CtExpression defaultExpression = element.getDefaultExpression();
		if(defaultExpression instanceof CtNewClass) {
			return;
		}
		invoc.setArguments(Arrays.asList(new Object[]{defaultExpression}));

		if(defaultExpression !=null &&
				defaultExpression.getType() != null &&
				defaultExpression.getType().isPrimitive() &&
				!defaultExpression.toString().equals("null")) {
			CtTypeReference destType = element.getType();
			CtTypeReference expressionType = defaultExpression.getType();
			List typeCasts = defaultExpression.getTypeCasts();
			if(typeCasts.size() > 0) {
				expressionType = ((CtTypeReference) typeCasts.get(typeCasts.size() - 1));
			}
			defaultExpression.addTypeCast(destType);
		}
		element.setDefaultExpression(invoc);


		/*
		// assign Integer in int
		if(element.getType() != null &&
				element.getAssignment() != null &&
				element.getAssignment().getType() != null &&
				element.getType().isPrimitive() &&
				!element.getAssignment().getType().isPrimitive()) {

			CtExecutableReference isCalledExec = getFactory().Core().createExecutableReference();
			isCalledExec.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			isCalledExec.setSimpleName("isCalled");
			isCalledExec.setStatic(true);

			CtTypeReference targetType = element.getDefaultExpression().getType();
			CtFieldReference<Object> ctfe = getFactory().Core().createFieldReference();
			ctfe.setSimpleName("class");
			ctfe.setDeclaringType(targetType.box());
			ctfe.setType(getFactory().Code().createCtTypeReference(Class.class));

			CtFieldRead<Object> arg = getFactory().Core().createFieldRead();
			arg.setVariable(ctfe);

			CtInvocationImpl invocCalled = (CtInvocationImpl) getFactory().Core().createInvocation();
			invocCalled.setExecutable(isCalledExec);
			invocCalled.setArguments(Arrays.asList(new CtExpression[]{element.getDefaultExpression(), arg}));
			invocCalled.setType(targetType);
			element.setDefaultExpression(invocCalled);
		}*/
	}

}
