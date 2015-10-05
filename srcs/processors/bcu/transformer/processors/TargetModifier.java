package bcu.transformer.processors;

import java.util.Arrays;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtGenericElementReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtVariableAccessImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

@SuppressWarnings("all")
public class TargetModifier extends AbstractProcessor<CtTargetedExpression>{

	private int i=0;
	private int j=0;
	@Override
	public void processingDone() {
		System.out.println("target-->"+i +" (failed:"+j+")");
	}
	@Override
	public void process(CtTargetedExpression element) {
		if(element.getTarget()==null)
			return;
		if(element instanceof CtFieldAccess<?> && ((CtFieldAccess) element).getVariable().isStatic())
			return;
		if(element.getTarget() instanceof CtThisAccess || element.getTarget() instanceof CtSuperAccess)
			return;
		
		String sign = "";
		if (element.getTarget() instanceof CtVariableAccess) {
			sign = ((CtVariableAccess)element.getTarget()).getVariable().getSimpleName();
		}
		try{
			i++;
			CtTypeReference tmp = element.getTarget().getType();
			if(sign.equals("class")){
				tmp = getFactory().Type().createReference(Class.class);
			}
			if(element.getTarget().getTypeCasts()!=null && element.getTarget().getTypeCasts().size()>0){
				tmp = (CtTypeReference) element.getTarget().getTypeCasts().get(0);
			}
			
			CtExpression arg = null;
			if(tmp instanceof CtArrayTypeReference){
				tmp=((CtArrayTypeReference)tmp).getDeclaringType();
			}
			if((tmp instanceof CtGenericElementReference)) {
				return;
			}
			if(tmp==null || tmp.isAnonymous() || tmp.getSimpleName()==null || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			}else{
				tmp = getFactory().Type().createReference(tmp.getQualifiedName());
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(tmp.box());
				
				arg = new CtFieldAccessImpl();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}
			
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference("bcornu.resi.CallChecker"));
			execref.setSimpleName("isCalled");
			execref.setStatic(true);
			
			CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{element.getTarget(),arg}));
			
			element.setTarget((CtExpression)invoc);
			

			
		}catch(Throwable t){
			t.printStackTrace();
			j++;
		}
	}

}
