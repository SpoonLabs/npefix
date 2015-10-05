package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtExecutableReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;

public class VarRetrieveAssign extends AbstractProcessor<CtAssignment>  {

	int i=0;
	int j=0;
	
	@Override
	public void process(CtAssignment element) {
		try{
			j++;
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			execref.setSimpleName("varAssign");
			execref.setStatic(true);
			
			CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getAssigned()}));
			
			element.insertAfter(invoc);
		}catch(Throwable t){
			i++;
		}
	}
	
	@Override
	public void processingDone() {
		System.out.println("assign --> "+j+" (failed: "+i+")");
	}

}
