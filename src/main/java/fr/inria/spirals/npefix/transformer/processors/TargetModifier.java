package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.*;
import spoon.support.reflect.code.CtInvocationImpl;

import java.util.Arrays;
import java.util.Set;

import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.createCtTypeElement;
import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.isStaticAndFinal;

@SuppressWarnings("all")
public class TargetModifier extends AbstractProcessor<CtTargetedExpression>{
	private int i=0;
	private int j=0;
	@Override
	public void processingDone() {
		System.out.println("target-->"+i +" (failed:"+j+")");
	}

	@Override
	public boolean isToBeProcessed(CtTargetedExpression element) {
		CtExpression target = element.getTarget();
		if (target == null)
			return false;
		if (isStaticAndFinal(element))
			return false;
		if (target instanceof CtThisAccess || target instanceof CtSuperAccess)
			return false;
		if (element.toString().startsWith(CallChecker.class.getSimpleName())) {
			return false;
		}
		if (element.getType() instanceof CtTypeParameterReference) {
			return false;
		}
		if (target.getType() instanceof CtTypeParameterReference) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtTargetedExpression element) {
		CtExpression target = element.getTarget();
		String sign = "";
		CtVariableReference variable = null;
		if (target instanceof CtVariableAccess) {
			variable = ((CtVariableAccess) target).getVariable();
			sign = variable.getSimpleName();
		}
		try{
			i++;
			CtTypeReference targetType = target.getType();
			if(sign.equals("class")){
				targetType = getFactory().Type().createReference(Class.class);
				return;
			}
			if(target.getTypeCasts() != null && target.getTypeCasts().size() > 0){
				targetType = (CtTypeReference) target.getTypeCasts().get(0);
			}
			if((targetType instanceof CtTypeParameterReference)) {
				return;
			}
			if(targetType == null && target instanceof CtVariableAccess) {
				CtVariableReference variable1 = ((CtVariableAccess) target).getVariable();
				if(variable1 != null) {
					targetType = variable1.getType();
				}
			}

			CtExpression ctTargetType = createCtTypeElement(targetType);
			if(ctTargetType == null) {
				return;
			}
			
			CtExecutableReference execref = getFactory().Core().createExecutableReference();
			execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			execref.setSimpleName("isCalled");
			execref.setStatic(true);
			
			CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			invoc.setExecutable(execref);
			invoc.setArguments(Arrays.asList(new CtExpression[]{target, ctTargetType}));
			invoc.setType(targetType);
			element.setTarget((CtExpression) invoc);


			createBeforeCall(element, target, variable, ctTargetType);
		}catch(Throwable t){
			t.printStackTrace();
			j++;
		}
	}

	private boolean createBeforeCall(CtTargetedExpression element, CtExpression target, CtVariableReference variable, CtExpression arg) {
		CtExecutableReference beforecallRef = getFactory().Core().createExecutableReference();
		beforecallRef.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
		beforecallRef.setSimpleName("beforeCalled");
		beforecallRef.setStatic(true);

		if(target instanceof CtArrayRead) {
            target = getFactory().Core().clone(target);
            target.getTypeCasts().clear();

            CtInvocation beforeCall = getFactory().Code().createInvocation(null, beforecallRef, target, arg);
            beforeCall.setType(target.getType());

            CtAssignment variableAssignment = ((CtAssignment) getFactory().Core().createAssignment().setAssignment(beforeCall)).setAssigned(target);

            CtElement parent = element;
            if(parent.getParent(CtStatementList.class) == null) {
				return true;
            }
            while (!(parent.getParent() instanceof CtStatementList)) {
                parent = parent.getParent();
            }
            // first contructor line
            if(parent.getParent(CtConstructor.class) != null &&
					parent instanceof CtInvocation &&
					((CtInvocation)parent).getExecutable().getSimpleName().startsWith("<init>")) {
				return true;
            }
            ((CtStatement)parent).insertBefore(variableAssignment);
            variableAssignment.setParent(parent.getParent());
        } else if(target instanceof CtVariableAccess) {
            if(variable instanceof CtFieldReference) {
                if(((CtFieldReference)variable).isFinal()) {
					return true;
                }
            }
            Set<ModifierKind> modifiers = variable.getModifiers();
            if(modifiers.contains(ModifierKind.FINAL)) {
				return true;
            }
            if(variable.getDeclaration() == null) {
				return true;
            }
            if(variable.getDeclaration().getParent() instanceof CtLoop) {
				return true;
            }
            target = getFactory().Core().clone(target);
            target.getTypeCasts().clear();
            CtInvocation beforeCall = getFactory().Code().createInvocation(null, beforecallRef, target, arg);
            beforeCall.setType(target.getType());
            for (int k = 0; k < target.getTypeCasts().size(); k++) {
                Object o = target.getTypeCasts().get(k);
                beforeCall.setType((CtTypeReference) o);
            }
            boolean isStatic = false;
            if(variable instanceof CtFieldReference) {
                isStatic = ((CtFieldReference)variable).isStatic();
                if(((CtFieldReference) variable).isFinal()) {
					return true;
                }
            }
            CtAssignment variableAssignment = ((CtAssignment) getFactory().Core().createAssignment().setAssignment(beforeCall)).setAssigned(target);
            CtElement parent = element;
            if(parent.getParent(CtStatementList.class) == null) {
				return true;
            }
            while (!(parent.getParent() instanceof CtStatementList)) {
                parent = parent.getParent();
            }
            // first contructor line
            if(parent.getParent(CtConstructor.class) != null && parent instanceof CtInvocation && ((CtInvocation)parent).getExecutable().getSimpleName().startsWith("<init>")) {
				return true;
            }
            try {
                if(!variable.getDeclaration().getType().getActualTypeArguments().get(0).getSimpleName().equals("?") &&
                        (target instanceof CtFieldAccess) &&
                        ((CtFieldAccess) target).getTarget() != null &&
                        !(((CtFieldAccess) target).getTarget() instanceof CtThisAccess)) {
                    System.err.println("test");
					return true;
                }
            } catch (Exception e) {

            }

            ((CtStatement)parent).insertBefore(variableAssignment);
            variableAssignment.setParent(parent.getParent());
        }
		return false;
	}

}
