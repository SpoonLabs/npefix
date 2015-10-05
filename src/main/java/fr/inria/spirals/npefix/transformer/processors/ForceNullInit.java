package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

import java.util.Arrays;

/**
 * @author bcornu
 *
 */
@SuppressWarnings("all")
public class ForceNullInit extends AbstractProcessor<CtLocalVariable> {

	@Override
	public void process(CtLocalVariable element) {
		if(element.getDefaultExpression()!=null 
				|| element.getParent() instanceof CtForEach)
			return;
		if(element.hasModifier(ModifierKind.FINAL)){
			element.removeModifier(ModifierKind.FINAL);
		}
		CtTypeReference tmp2 = element.getType();
		
		CtExpression arg = null;
		if(tmp2 instanceof CtArrayTypeReference){
			tmp2=((CtArrayTypeReference)tmp2).getDeclaringType();
		}
		if(tmp2==null || tmp2.isAnonymous() || tmp2.getSimpleName()==null || (tmp2.getPackage()==null && tmp2.getSimpleName().length()==1)){
			arg = getFactory().Core().createLiteral();
			arg.setType(getFactory().Type().nullType());
		}else{
			tmp2 = getFactory().Type().createReference(tmp2.getQualifiedName());
			CtFieldReference ctfe = new CtFieldReferenceImpl();
			ctfe.setSimpleName("class");
			ctfe.setDeclaringType(tmp2);
			
			arg = new CtFieldAccessImpl();
			((CtFieldAccessImpl) arg).setVariable(ctfe);
		}
		
		CtExecutableReference execref = getFactory().Core().createExecutableReference();
		execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
		execref.setSimpleName("init");
		execref.setStatic(true);
		
		CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		invoc.setExecutable(execref);
		invoc.setArguments(Arrays.asList(new CtExpression[]{arg}));
		
		element.setDefaultExpression(invoc);
	}

}
