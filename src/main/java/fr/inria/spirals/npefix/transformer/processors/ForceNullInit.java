package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;

import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.createCtTypeElement;

/**
 * @author bcornu
 *
 */
@SuppressWarnings("all")
public class ForceNullInit extends AbstractProcessor<CtLocalVariable> {

	@Override
	public boolean isToBeProcessed(CtLocalVariable element) {
		if((element.getDefaultExpression()!=null)
				|| element.getParent() instanceof CtForEach)
			return false;
		return super.isToBeProcessed(element);
	}

	@Override
	public void process(CtLocalVariable element) {
		if(element.hasModifier(ModifierKind.FINAL)){
			element.removeModifier(ModifierKind.FINAL);
		}
		CtTypeReference tmp2 = element.getType();

		CtExpression arg = createCtTypeElement(tmp2);
		if(arg == null) {
			return;
		}
		
		CtExecutableReference execref = getFactory().Core().createExecutableReference();
		execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
		execref.setSimpleName("init");
		execref.setStatic(true);
		
		CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		element.setDefaultExpression(invoc);
		invoc.setExecutable(execref);
		invoc.setArguments(Arrays.asList(new CtExpression[]{arg}));
	}

}
