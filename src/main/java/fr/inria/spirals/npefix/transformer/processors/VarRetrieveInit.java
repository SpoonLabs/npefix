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
		invoc.setArguments(Arrays.asList(new CtExpression[]{element.getDefaultExpression()}));
		
		element.setDefaultExpression(invoc);
	}

}
