package bcu.transformer.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtVariableAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;
import bcu.transformer.utils.IConstants;

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
		execref.setDeclaringType(getFactory().Type().createReference("bcornu.resi.CallChecker"));
		execref.setSimpleName("init");
		execref.setStatic(true);
		
		CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
		invoc.setExecutable(execref);
		invoc.setArguments(Arrays.asList(new CtExpression[]{arg}));
		
		element.setDefaultExpression(invoc);
	}

}
