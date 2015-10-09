package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.reference.*;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

import java.util.Arrays;

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
		if(element instanceof CtInvocationImpl<?> && ((CtInvocationImpl) element).getExecutable().isStatic())
			return;
		if(element.getTarget() instanceof CtThisAccess || element.getTarget() instanceof CtSuperAccess)
			return;
		if(element.toString().startsWith(CallChecker.class.getSimpleName())) {
			return;
		}
		if(element.getType() instanceof CtTypeParameterReference) {
			return;
		}
		if(element.getTarget().getType() instanceof CtTypeParameterReference) {
			return;
		}

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
			if((tmp instanceof CtTypeParameterReference)) {
				return;
			}
			if(tmp==null || tmp.isAnonymous() || tmp.getSimpleName()==null || (tmp.getPackage()==null && tmp.getSimpleName().length()==1)){
				arg = getFactory().Core().createLiteral();
				arg.setType(getFactory().Type().nullType());
			} else {
				if(tmp instanceof CtArrayTypeReference){
					if(((CtArrayTypeReference) tmp).getComponentType() instanceof CtTypeParameterReference) {
						return;
					}
					tmp = getFactory().Type().createReference(((CtArrayTypeReference) tmp).getComponentType().getQualifiedName() + "[]");
				} else {
					tmp = getFactory().Type().createReference(tmp.getQualifiedName());
				}
				CtFieldReference ctfe = new CtFieldReferenceImpl();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(tmp.box());
				
				arg = new CtFieldAccessImpl();
				((CtFieldAccessImpl) arg).setVariable(ctfe);
			}

			
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
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
