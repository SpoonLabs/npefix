package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.reference.CtTypeReference;

import java.util.Date;

public class VarRetrieveInit extends AbstractProcessor<CtLocalVariable>  {

	private Date start;
	private int nbVarInit;

	@Override
	public void init() {
		this.start = new Date();
	}

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
		CtExpression defaultExpression = element.getDefaultExpression();
		if(defaultExpression instanceof CtNewClass) {
			return false;
		}
		if (defaultExpression == null) {
			return false;
		}
		if(element.getParent(CtLambda.class) != null) {
			return false;
		}
		if (defaultExpression.toString().contains("CallChecker.init")) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtLocalVariable element) {
		nbVarInit++;

		CtExpression defaultExpression = element.getDefaultExpression().clone();

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		CtLiteral<String> variableName = getFactory().Code()
				.createLiteral(element.getSimpleName());

		CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(),
				CallChecker.class,
				"varInit",
				defaultExpression,
				variableName,
				lineNumber,
				sourceStart,
				sourceEnd);
		invoc.setPosition(element.getPosition());

		if(defaultExpression !=null &&
				defaultExpression.getType() != null &&
				defaultExpression.getType().isPrimitive() &&
				!defaultExpression.toString().equals("null")) {
			CtTypeReference destType = element.getType();
			defaultExpression.addTypeCast(destType.clone());
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


	@Override
	public void processingDone() {
		System.out.println("VarInit --> "+ nbVarInit + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

}
