package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.SpoonException;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.*;
import spoon.support.reflect.code.CtFieldAccessImpl;
import spoon.support.reflect.code.CtFieldReadImpl;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

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
		CtExpression target = element.getTarget();
		if(target ==null)
			return;
		if(element instanceof CtFieldAccess<?> && ((CtFieldAccess) element).getVariable().isStatic())
			return;
		if(element instanceof CtInvocationImpl<?> && ((CtInvocationImpl) element).getExecutable().isStatic())
			return;
		if(target instanceof CtThisAccess || target instanceof CtSuperAccess)
			return;
		if(element.toString().startsWith(CallChecker.class.getSimpleName())) {
			return;
		}
		if(element.getType() instanceof CtTypeParameterReference) {
			return;
		}
		if(target.getType() instanceof CtTypeParameterReference) {
			return;
		}

		String sign = "";
		CtVariableReference variable = null;
		if (target instanceof CtVariableAccess) {
			variable = ((CtVariableAccess) target).getVariable();
			sign = variable.getSimpleName();
		}
		try{
			i++;
			CtTypeReference tmp = target.getType();
			if(sign.equals("class")){
				tmp = getFactory().Type().createReference(Class.class);
				return;
			}
			if(target.getTypeCasts()!=null && target.getTypeCasts().size()>0){
				tmp = (CtTypeReference) target.getTypeCasts().get(0);
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
				CtFieldReference<Object> ctfe = getFactory().Core().createFieldReference();
				ctfe.setSimpleName("class");
				ctfe.setDeclaringType(tmp.box());
				ctfe.setType(getFactory().Code().createCtTypeReference(Class.class));
				
				arg = getFactory().Core().createFieldRead();
				((CtFieldAccess) arg).setVariable(ctfe);
			}
			arg.getTypeCasts().clear();

			
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			execref.setSimpleName("isCalled");
			execref.setStatic(true);
			
			CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{target, arg}));
			
			element.setTarget((CtExpression) invoc);


			CtExecutableReference beforecallRef = getFactory().Core().createExecutableReference();
			beforecallRef.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			beforecallRef.setSimpleName("beforeCalled");
			beforecallRef.setStatic(true);


			if(target instanceof CtVariableAccess) {
				if(variable instanceof CtFieldReference) {
					if(((CtFieldReference)variable).isFinal()) {
						return;
					}
				}
				Set<ModifierKind> modifiers = variable.getModifiers();
				if(modifiers.contains(ModifierKind.FINAL)) {
					return;
				}
				if(variable.getDeclaration() == null) {
					return;
				}
				if(variable.getDeclaration().getParent() instanceof CtLoop) {
					return;
				}
				target = getFactory().Core().clone(target);
				target.getTypeCasts().clear();
				CtInvocation beforeCall = getFactory().Code().createInvocation(null, beforecallRef, target, arg);
				beforeCall.setType(target.getType());
				boolean isStatic = false;
				if(variable instanceof CtFieldReference) {
					isStatic = ((CtFieldReference)variable).isStatic();
					if(((CtFieldReference) variable).isFinal()) {
						return;
					}
				}
				CtAssignment variableAssignment = ((CtAssignment) getFactory().Core().createAssignment().setAssignment(beforeCall)).setAssigned(target);
				CtElement parent = element;
				if(parent.getParent(CtStatementList.class) == null) {
					return;
				}
				while (!(parent.getParent() instanceof CtStatementList)) {
					parent = parent.getParent();
				}
				// first contructor line
				if(parent.getParent(CtConstructor.class) != null && parent instanceof CtInvocation && ((CtInvocation)parent).getExecutable().getSimpleName().startsWith("<init>")) {
					return;
				}
				((CtStatement)parent).insertBefore(variableAssignment);
				variableAssignment.setParent(parent.getParent());
			}

		}catch(Throwable t){
			t.printStackTrace();
			j++;
		}
	}

}
