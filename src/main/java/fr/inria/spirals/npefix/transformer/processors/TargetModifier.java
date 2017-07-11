package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtArrayRead;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtCatchVariableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;

import java.util.Date;
import java.util.Set;

import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.createCtTypeElement;
import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.isStaticAndFinal;

@SuppressWarnings("all")
public class TargetModifier extends AbstractProcessor<CtTargetedExpression>{
	private Date start;
	private int i=0;
	private int j=0;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("target--> " + i + " (failed:"+j+")"+ " in " + (new Date().getTime() - start.getTime()) + "ms");
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
			//return false;
		}
		if (element.getType() instanceof CtTypeParameterReference) {
			return false;
		}
		if (target.getType() instanceof CtTypeParameterReference) {
			return false;
		}
		if(element.getParent(CtLambda.class) != null) {
			return false;
		}
		if(target instanceof CtVariableRead) {
			if (((CtVariableRead)target).getVariable() instanceof CtCatchVariableReference) {
				return false;
			}
		}
		if(target instanceof CtThisAccess) {
			return false;
		}
		if(target instanceof CtTypeAccess) {
			return false;
		}
		if(element.getPosition() == null) {
			return false;
		}
		if (target.getMetadata("notnull") != null) {
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

			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(target.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(target.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(target.getPosition().getSourceEnd());

			CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"isCalled",
					target,
					ctTargetType,
					lineNumber,
					sourceStart,
					sourceEnd);
			invoc.setPosition(element.getPosition());
			invoc.setType(targetType);

			element.setTarget(invoc);

			createBeforeCall(element, target, variable, ctTargetType);
		}catch(Throwable t){
			t.printStackTrace();
			j++;
		}
	}

	private boolean createBeforeCall(CtTargetedExpression element, CtExpression target, CtVariableReference variable, CtExpression arg) {
		if (element.getParent(CtField.class) != null) {
			return false;
		}
		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(target.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(target.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(target.getPosition().getSourceEnd());

		if(target instanceof CtArrayRead) {
            target = target.clone();
            target.getTypeCasts().clear();

			CtInvocation beforeCall = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"beforeCalled",
					target,
					arg,
					lineNumber,
					sourceStart,
					sourceEnd);
            beforeCall.setType(target.getType());
			beforeCall.setPosition(element.getPosition());

            CtAssignment variableAssignment = ((CtAssignment) getFactory().Core().createAssignment()
					.setAssignment(beforeCall))
					.setAssigned(target);
			variableAssignment.setPosition(element.getPosition());

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
			if(variable.getDeclaration() instanceof CtCatchVariable) {
				if(((CtCatchVariable) variable.getDeclaration()).getMultiTypes().size() > 1) {
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


			CtInvocation beforeCall = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"beforeCalled",
					target,
					arg,
					lineNumber,
					sourceStart,
					sourceEnd);

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
			variableAssignment.setPosition(element.getPosition());

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
					return true;
                }
            } catch (Exception e) {

            }
			try{
				((CtStatement)parent).insertBefore(variableAssignment);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

            variableAssignment.setParent(parent.getParent());
        }
		return false;
	}

}
