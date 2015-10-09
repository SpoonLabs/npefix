package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class VarRetrieveInit extends AbstractProcessor<CtLocalVariable>  {

	@Override
	public void process(CtLocalVariable element) {

		if(element.getParent() instanceof CtForEach
				|| element.getParent() instanceof CtFor)
			return;
		if(element.getType().getPackage()!=null && element.getType()
				.getPackage()
				.toString()
				.startsWith("fr.inria.spirals.npefix")) {
			return;
		}
		if (element.getSimpleName().startsWith("npe_")) {
			return;
		}
		
		CtExecutableReference execref = getFactory().Core().createExecutableReference();
		execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
		execref.setSimpleName("varInit");
		execref.setStatic(true);
		
		CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		invoc.setExecutable(execref);
		CtLiteral<String> variableName = getFactory().Code().createLiteral(element.getSimpleName());
		CtExpression defaultExpression = element.getDefaultExpression();
		invoc.setArguments(Arrays.asList(new Object[]{variableName, defaultExpression}));
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
	}

}
