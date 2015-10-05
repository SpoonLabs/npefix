package processors.bcu.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;

public class VarRetrieveInit extends AbstractProcessor<CtLocalVariable>  {

	@Override
	public void process(CtLocalVariable element) {

		if(element.getParent() instanceof CtForEach
				|| element.getParent() instanceof CtFor)
			return;
		
		CtExecutableReference execref = getFactory().Core().createExecutableReference();
		execref.setDeclaringType(getFactory().Type().createReference("bcornu.resi.CallChecker"));
		execref.setSimpleName("varInit");
		execref.setStatic(true);
		
		CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		invoc.setExecutable(execref);
		invoc.setArguments(Arrays.asList(new CtExpression[]{element.getDefaultExpression()}));
		
		element.setDefaultExpression(invoc);
	}

}
