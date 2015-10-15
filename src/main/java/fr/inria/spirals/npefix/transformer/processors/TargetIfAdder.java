package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.AbnormalExecutionError;
import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.*;
import spoon.support.reflect.code.*;
import spoon.support.reflect.reference.CtFieldReferenceImpl;

import java.lang.reflect.Array;
import java.util.*;

@SuppressWarnings("all")
public class TargetIfAdder extends AbstractProcessor<CtTargetedExpression>{

	private int i=0;
	private int j=0;

	private Map<String, String> invocationVariables = new HashMap<>();

	@Override
	public void processingDone() {
		System.out.println("if -->"+i +" (failed:"+j+")");
	}
	

	@Override
	public void process(CtTargetedExpression element) {
		CtExpression target = element.getTarget();
		if(target == null)
			return;
		if(element instanceof CtFieldAccess<?> && ((CtFieldAccess) element).getVariable().isStatic())
			return;
		if(element instanceof CtInvocationImpl<?> && ((CtInvocationImpl) element).getExecutable().isStatic())
			return;
		if(target instanceof CtThisAccess
				|| target instanceof CtSuperAccess
				|| target instanceof CtTypeAccess)
			return;

		if(element.getParent() instanceof CtBinaryOperator && element.getParent(CtReturn.class) != null ) {
			return;
		}

		CtElement line = element;
		try{
			i++;
			CtElement parent = null;
			boolean found = false;
			boolean needElse = false;
			try{
				while (!found) {
					parent=line.getParent();
					if(parent == null || parent.getParent()==null){
						return;
					}else if(parent.getParent() instanceof CtConstructor && line instanceof CtInvocation 
							&& ((CtInvocation)line).getExecutable().getSimpleName().equals("<init>")){
						return;//premiere ligne constructeur
					}else if(parent instanceof CtReturn || parent instanceof CtThrow){
						line=parent;
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
						needElse = true;
						found=true;
					}else if(parent instanceof CtAssignment 
							&& ((CtAssignment) parent).getAssigned() instanceof CtFieldAccess){
						return;
					}else if(parent instanceof CtLoop){
						return;
					}else{
						line=parent;
					}
				}
			}catch(ParentNotInitializedException pni){
				System.err.println(line);
				pni.printStackTrace();
				j++;
			}
			if(line instanceof CtLocalVariable && ((CtLocalVariable) line).hasModifier(ModifierKind.FINAL))
				return;

			if(target instanceof CtInvocation) {
				int id = invocationVariables.size();
				String variableName = "npe_invocation_var" + id;
				CtExpression localTarget = getFactory().Core().clone(target);

				CtTypeReference type = localTarget.getType();
				List<CtTypeReference> typeCasts = localTarget.getTypeCasts();
				// use cast
				if(typeCasts.size() > 0) {
					type = typeCasts.get(typeCasts.size() - 1);
				}
				//type.getActualTypeArguments().clear();
				//localTarget.addTypeCast(type);
				CtLocalVariable localVariable = getFactory().Code().createLocalVariable(type, variableName, localTarget);
				if(line instanceof CtStatement) {
					((CtStatement)line).insertBefore(localVariable);
					localVariable.setParent(line.getParent());
				}
				target = getFactory().Code().createVariableRead(localVariable.getReference(), false);
				element.setTarget(target);
				invocationVariables.put(target.toString(), variableName);
			}

			
			CtExecutableReference execif = getFactory().Core().createExecutableReference();
			execif.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
			execif.setSimpleName("beforeDeref");
			execif.setStatic(true);

			CtInvocationImpl ifInvoc = (CtInvocationImpl) getFactory().Core().createInvocation();
			ifInvoc.setExecutable(execif);
			ifInvoc.setArguments(Arrays.asList(new CtExpression[]{target}));
			
			CtIf encaps = getFactory().Core().createIf();
			CtElement directParent = element.getParent();
			encaps.setParent(line.getParent());
			if(directParent instanceof CtConditional) {
				if(element.equals(((CtConditional)directParent).getElseExpression())) {
					CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(((CtConditional) directParent).getCondition(), ifInvoc, BinaryOperatorKind.OR);
					encaps.setCondition(binaryOperator);
				} else {
					CtUnaryOperator<Object> unaryOperator1 = getFactory().Core().createUnaryOperator();
					unaryOperator1.setKind(UnaryOperatorKind.NOT);
					unaryOperator1.setOperand(((CtConditional) directParent).getCondition());
					CtBinaryOperator binaryOperator = getFactory().Code().createBinaryOperator(unaryOperator1, ifInvoc, BinaryOperatorKind.OR);
					encaps.setCondition(binaryOperator);
				}
			} else {
				encaps.setCondition(ifInvoc);
			}


			CtBlock thenBloc = getFactory().Core().createBlock();

			//add var init
			if(line instanceof CtLocalVariable){
				CtLocalVariable localVar = (CtLocalVariable) line;
				
				CtAssignment assign = getFactory().Core().createAssignment();

				CtVariableAccess va = getFactory().Core().createVariableWrite();
				va.setVariable(localVar.getReference());
				
				assign.setAssigned(va);
				assign.setAssignment(localVar.getDefaultExpression());

				CtLocalVariable previous = getFactory().Core().createLocalVariable();
				previous.setType(localVar.getType());
				previous.setSimpleName(localVar.getSimpleName());
				
				CtTypeReference tmp2 = localVar.getType();
				
				CtExpression arg = null;

				if(tmp2==null || tmp2.isAnonymous() || tmp2.getSimpleName()==null || (tmp2.getPackage()==null && tmp2.getSimpleName().length()==1)){
					arg = getFactory().Core().createLiteral();
					arg.setType(getFactory().Type().nullType());
				}else{
					if(tmp2 instanceof CtArrayTypeReference){
						if(((CtArrayTypeReference) tmp2).getComponentType() instanceof CtTypeParameterReference) {
							tmp2 = getFactory().Code().createCtTypeReference(Array.class);
						} else {
							tmp2 = getFactory().Type().createReference(((CtArrayTypeReference) tmp2).getComponentType().getQualifiedName() + "[]");
						}
					} else {
						tmp2 = getFactory().Type().createReference(tmp2.getQualifiedName());
					}
					CtFieldReference ctfe = new CtFieldReferenceImpl();
					ctfe.setSimpleName("class");
					ctfe.setDeclaringType(tmp2);
					
					arg = getFactory().Core().createFieldRead();
					((CtFieldAccess) arg).setVariable(ctfe);
					arg.setType(getFactory().Code().createCtTypeReference(Class.class));
				}
				CtExecutableReference execref = getFactory().Core().createExecutableReference();
				execref.setDeclaringType(getFactory().Type().createReference(CallChecker.class));
				execref.setSimpleName("init");
				execref.setStatic(true);
				
				CtInvocationImpl invoc = (CtInvocationImpl) getFactory().Core().createInvocation();
				invoc.setExecutable(execref);
				invoc.setArguments(Arrays.asList(new CtExpression[]{arg}));
				
				previous.setDefaultExpression(invoc);
				
				((CtLocalVariable) line).insertBefore(previous);
				previous.setParent(line.getParent());
				
				thenBloc.addStatement(assign);
				assign.setParent(thenBloc);
				encaps.setThenStatement(thenBloc);
				((CtLocalVariable) line).replace(encaps);
			} else if(line instanceof CtStatement){
				((CtStatement) line).replace(encaps);
				encaps.setThenStatement(thenBloc);
				thenBloc.addStatement((CtStatement)line);
				line.setParent(thenBloc);
			}

			CtMethod methodParent = encaps.getParent(CtMethod.class);
			if(methodParent != null) {
				CtStatement lastStatement = methodParent.getBody().getLastStatement();
				if (lastStatement.equals(encaps) && !methodParent.getType().toString().equals("void")) {
					needElse = true;
				}
			}

			if(needElse){
				CtConstructorCall npe = getFactory().Core().createConstructorCall();
				npe.setType(getFactory().Type().createReference(AbnormalExecutionError.class));
				
				CtThrow thrower = getFactory().Core().createThrow();
				thrower.setThrownExpression(npe);
				
				encaps.setElseStatement(thrower);
			}
			
		}catch(Throwable t){
			System.out.println(line+"-->"+element);
			t.printStackTrace();
			j++;
		}
	}

}
