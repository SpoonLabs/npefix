package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.exception.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.exception.NPEFixError;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.declaration.ParentNotInitializedException;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtCatchVariableReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.createCtTypeElement;
import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.removeUnaryOperator;

@SuppressWarnings("all")
public class BeforeDerefAdder extends AbstractProcessor<CtTargetedExpression>{

	private int nbBeforeDeref =0;
	private int countFailed =0;

	private Map<String, String> invocationVariables = new HashMap<>();

	@Override
	public void processingDone() {
		System.out.println("if --> " + nbBeforeDeref + " (failed:" + countFailed + ")");
	}

	@Override
	public boolean isToBeProcessed(CtTargetedExpression element) {
		CtExpression target = element.getTarget();
		if(target == null)
			return false;
		if(ProcessorUtility.isStatic(element))
			return false;
		if(target instanceof CtThisAccess
				|| target instanceof CtSuperAccess
				|| target instanceof CtTypeAccess)
			return false;

		if(element.getParent() instanceof CtBinaryOperator &&
				element.getParent(CtReturn.class) != null ) {
			return false;
		}
		if(target instanceof CtVariableRead) {
			if (((CtVariableRead)target).getVariable() instanceof CtCatchVariableReference) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void process(CtTargetedExpression element) {
		CtExpression target = getFactory().Core().clone(element.getTarget());

		CtElement line = element;
		try{
			CtElement parent = null;
			boolean found = false;
			boolean needElse = false;
			try{
				while (!found) {
					parent = line.getParent();
					if(parent == null || parent.getParent() == null){
						return;
					}else if(parent.getParent() instanceof CtConstructor && line instanceof CtInvocation 
							&& ((CtInvocation)line).getExecutable().getSimpleName().equals("<init>")){
						return;//first line constructor
					}else if(parent instanceof CtReturn || parent instanceof CtThrow){
						line = parent;
						needElse = true;
					}else if(parent instanceof CtBlock){
						found=true;
					}else if(parent instanceof CtCase){
						needElse = true;
						found=true;
					}else if(parent instanceof CtStatementList){
						found=true;
					}else if(parent instanceof CtCatch){
						found=true;
					}else if(parent instanceof CtIf){
						line=parent;
						//needElse = true;
						found=true;
					}else if(parent instanceof CtAssignment
							&& ((CtAssignment) parent).getAssigned() instanceof CtFieldAccess
							&& ((CtFieldAccess)((CtAssignment) parent).getAssigned()).getVariable().isFinal()){
						return;
					} else if(parent instanceof CtLoop){
						return;
					}else{
						line=parent;
					}
				}
			} catch (ParentNotInitializedException pni){
				System.err.println(line);
				pni.printStackTrace();
				countFailed++;
				return;
			}
			if (line instanceof CtLocalVariable && ((CtLocalVariable) line).hasModifier(ModifierKind.FINAL))
				return;

			nbBeforeDeref++;

			// extract all invocations in order to avoid double method calls
			target = extractInvocations(element, target, line);
			// remove unary operators
			target = removeUnaryOperator(target, true);
			// remove casts
			target.getTypeCasts().clear();

			CtTypeReference targetType = target.getType();
			if(target.getTypeCasts() != null && target.getTypeCasts().size() > 0){
				targetType = (CtTypeReference) target.getTypeCasts().get(0);
			}
			if(targetType == null && target instanceof CtVariableAccess) {
				CtVariableReference variable1 = ((CtVariableAccess) target).getVariable();
				if(variable1 != null) {
					targetType = variable1.getType();
				}
			}
			CtExpression ctTargetType;
			if(!(targetType instanceof CtTypeParameterReference)) {
				ctTargetType = createCtTypeElement(targetType);
			} else {
				ctTargetType = getFactory().Code().createLiteral(null);
			}

			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

			CtInvocation beforeDerefInvocation = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"beforeDeref",
					target,
					ctTargetType,
					lineNumber,
					sourceStart,
					sourceEnd);
			
			final CtIf encaps = getFactory().Core().createIf();
			CtElement directParent = element.getParent();
			encaps.setParent(line.getParent());
			encaps.setPosition(element.getPosition());

			// handle ternary operator
			if(directParent instanceof CtConditional) {
				if(element.equals(((CtConditional)directParent).getElseExpression())) {
					CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(((CtConditional) directParent).getCondition(), beforeDerefInvocation, BinaryOperatorKind.OR);
					encaps.setCondition(binaryOperator);
					binaryOperator.setPosition(element.getPosition());
				} else {
					CtUnaryOperator<Object> unaryOperator1 = getFactory().Core().createUnaryOperator();
					unaryOperator1.setKind(UnaryOperatorKind.NOT);
					unaryOperator1.setOperand(((CtConditional) directParent).getCondition());
					unaryOperator1.setPosition(element.getPosition());
					CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(unaryOperator1, beforeDerefInvocation, BinaryOperatorKind.OR);
					encaps.setCondition(binaryOperator);
				}
			} else {
				encaps.setCondition(beforeDerefInvocation);
			}

			CtBlock thenBloc = getFactory().Core().createBlock();
			thenBloc.setPosition(element.getPosition());

			// split local variable declaration into two statements (declaration and initialization)
			if(line instanceof CtLocalVariable){
				CtLocalVariable localVar = (CtLocalVariable) line;
				
				CtAssignment variableInitialization = getFactory().Code().createVariableAssignment(localVar.getReference(), false, localVar.getDefaultExpression());
				variableInitialization.setPosition(element.getPosition());

				CtExpression arg = createCtTypeElement(localVar.getType());
				if(arg == null) {
					return;
				}

				CtInvocation initializationExpression = ProcessorUtility.createStaticCall(getFactory(), CallChecker.class, "init", arg);

				CtLocalVariable variableDeclaration = getFactory().Code().createLocalVariable(localVar.getType(), localVar.getSimpleName(), initializationExpression);

				localVar.insertBefore(variableDeclaration);
				variableDeclaration.setParent(line.getParent());

				initializationExpression.setPosition(element.getPosition());
				variableDeclaration.setPosition(element.getPosition());

				variableInitialization.setParent(thenBloc);
				initializationExpression.setParent(variableDeclaration);

				thenBloc.addStatement(variableInitialization);

				encaps.setThenStatement(thenBloc);
				localVar.replace(encaps);
			} else if(line instanceof CtStatement){
				((CtStatement) line).replace(encaps);
				encaps.setThenStatement(thenBloc);
				thenBloc.addStatement((CtStatement)line);
				line.setParent(thenBloc);
			}

			CtTypedElement methodParent = encaps.getParent(CtMethod.class);
			if(methodParent == null) {
				methodParent = encaps.getParent(CtConstructor.class);
			}
			needElse = needElse(needElse, encaps, methodParent);

			// throw an exception after the condition
			if(needElse){
				CtConstructorCall npe = getFactory().Code().createConstructorCall(getFactory().Type().createReference(AbnormalExecutionError.class));
				npe.setPosition(element.getPosition());

				CtThrow thrower = getFactory().Core().createThrow();
				thrower.setThrownExpression(npe);
				thrower.setPosition(element.getPosition());
				
				encaps.setElseStatement(thrower);
			}
			
		} catch(Throwable t){
			System.err.println(line + "-->" + element);
			t.printStackTrace();
			countFailed++;
		}
	}

	private boolean needElse(boolean needElse, CtIf encaps, CtTypedElement methodParent) {
		if(!needElse && methodParent != null && !methodParent.getType().equals(getFactory().Type().VOID_PRIMITIVE)) {
			needElse = encaps.getElements(
					new AbstractFilter<CtReturn>(CtReturn.class) {
						@Override
						public boolean matches(CtReturn element) {
							return true;
						}
					}).size() > 0;
			if(!needElse) {
				needElse = encaps.getElements(
						new AbstractFilter<CtThrow>(CtThrow.class) {
							@Override
							public boolean matches(CtThrow element) {
								return !getFactory().Code().createCtTypeReference(
												NPEFixError.class).isAssignableFrom(
												element.getThrownExpression().getType()) && super.matches(element);
							}
						}).size() > 0;
			}
			if(!needElse) {
				needElse = encaps.getElements(
						new AbstractFilter<CtLocalVariable>(CtLocalVariable.class) {
							@Override
							public boolean matches(CtLocalVariable element) {
								return super.matches(element);
							}
						}).size() > 0;
			}
			if(!needElse && methodParent instanceof CtConstructor) {
				needElse = encaps.getElements(
						new AbstractFilter<CtFieldWrite>(CtFieldWrite.class) {
							@Override
							public boolean matches(CtFieldWrite element) {
								return element.getVariable().isFinal() && super.matches(element);
							}
						}).size() > 0;
			}
		}
		return needElse;
	}

	private CtExpression extractInvocations(CtTargetedExpression element,
			CtExpression target, CtElement line) {
		if(target instanceof CtInvocation) {
			int id = invocationVariables.size();
			String variableName = "npe_invocation_var" + id;
			CtExpression localTarget = getFactory().Core().clone(target);

			CtTypeReference type = localTarget.getType();
			if(type instanceof CtTypeParameterReference) {
				List<CtTypeReference<?>> bounds = ((CtTypeParameterReference) type)
						.getBounds();
				if(bounds.isEmpty()) {
					type = getFactory().Type().createReference(Object.class);
				} else {
					type = bounds.get(0);
				}
			}
			List<CtTypeReference> typeCasts = localTarget.getTypeCasts();
			// use cast
			if(typeCasts.size() > 0) {
				type = typeCasts.get(typeCasts.size() - 1);
			}
			removeGenType(type);
			CtLocalVariable localVariable = getFactory().Code().createLocalVariable(type, variableName, localTarget);
			localVariable.addModifier(ModifierKind.FINAL);
			if(line instanceof CtStatement) {
				((CtStatement)line).insertBefore(localVariable);
				localVariable.setParent(line.getParent());
			}
			target = getFactory().Code().createVariableRead(localVariable.getReference(), false);
			element.setTarget(target);
			invocationVariables.put(target.toString(), variableName);
		}
		return target;
	}

	private void removeGenType(CtTypeReference type) {
		if(type instanceof CtArrayTypeReference) {
			removeGenType(((CtArrayTypeReference) type).getComponentType());
		} else {
			List<CtTypeReference<?>> actualTypeArguments = new ArrayList<>(type
					.getActualTypeArguments());
			for (int i = 0; i < actualTypeArguments.size(); i++) {
				CtTypeReference<?> ctTypeReference = actualTypeArguments
						.get(i);
				if(ctTypeReference.toString().contains("?") && !(ctTypeReference.toString().contains("? extends") || ctTypeReference.toString().contains("? super"))) {
					type.getActualTypeArguments().remove(ctTypeReference);
				}
			}
		}
	}

}
