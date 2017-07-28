package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.MethodContext;
import fr.inria.spirals.npefix.resi.exception.ForceReturn;
import fr.inria.spirals.npefix.transformer.utils.IConstants;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * ajoute les try catch autour des methodes
 * @author bcornu
 *
 */
@SuppressWarnings("all")
public class MethodEncapsulation extends AbstractProcessor<CtMethod> {

	protected static int methodNumber = 0;
	protected Date start;

	public static int getCpt(){
		return methodNumber;
	}

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("MethodEncapsulation # Method: " + methodNumber + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtMethod ctMethode) {
		methodNumber++;
		if(ctMethode.getBody() == null)
			return false;
		if(ctMethode.getType() instanceof CtTypeParameterReference) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtMethod ctMethode) {
		collectFields(ctMethode);
		collectParams(ctMethode);
		collectThis(ctMethode);

		CtLocalVariable methodVar = getNewMethodcontext(ctMethode);
		methodVar.setPosition(ctMethode.getPosition());

		CtTypeReference tmpref = getFactory().Core().clone(ctMethode.getType());
		if(tmpref instanceof CtArrayTypeReference && ((CtArrayTypeReference)tmpref).getComponentType()!=null){
			((CtArrayTypeReference)tmpref).getComponentType().getActualTypeArguments().clear();
		}
		
		CtTry coreTry = createTry(methodVar, tmpref);
		if(coreTry == null) {
			return;
		}
		coreTry.setBody(getFactory().Core().createBlock());
		coreTry.getBody().setStatements(ctMethode.getBody().getStatements());
		
		List<CtStatement> stats = new ArrayList<CtStatement>();
		stats.add(methodVar);
		stats.add(coreTry);
		
		ctMethode.getBody().setStatements(stats);
	}

	protected CtTry createTry(CtLocalVariable methodVar, CtTypeReference tmpref){
		if((tmpref instanceof CtTypeParameterReference)) {
			return null;
		}

		CtCatchVariable parameter = getFactory().Code().createCatchVariable(getFactory().Type().createReference(ForceReturn.class), "_bcornu_return_t");
		parameter.setPosition(methodVar.getPosition());

		CtCatch localCatch = getFactory().Core().createCatch();
		localCatch.setParameter(parameter);
		localCatch.setBody(getFactory().Core().createBlock());

		CtVariableAccess methodAccess = getFactory().createVariableRead();
		methodAccess.setVariable(methodVar.getReference());
		
		CtReturn ret = getFactory().Core().createReturn();
		ret.setPosition(methodVar.getPosition());

		CtExpression arg = ProcessorUtility.createCtTypeElement(tmpref);

		CtExecutableReference<Object> getDecisionReference = getFactory().Core().createExecutableReference();
		getDecisionReference.setSimpleName("getDecision");
		getDecisionReference.setDeclaringType(getFactory().Code().createCtTypeReference(ForceReturn.class));

		CtExecutableReference<Object> getValueReference = getFactory().Core().createExecutableReference();
		getValueReference.setSimpleName("getValue");
		getValueReference.setDeclaringType(getFactory().Code().createCtTypeReference(Decision.class));

		CtInvocation invocReturn = getFactory().Code().createInvocation(
				getFactory().Code().createInvocation(
						getFactory().Code().createVariableRead(parameter.getReference(), false),
						getDecisionReference),
				getValueReference);
		//CtInvocation invocReturn = ProcessorUtility.createStaticCall(getFactory(), CallChecker.class, "returned", arg);
		invocReturn.setPosition(methodVar.getPosition());

		CtTypeReference variableType = tmpref;
		if(tmpref.equals(getFactory().Type().VOID_PRIMITIVE)){
			variableType = getFactory().Code().createCtTypeReference(Object.class);
			localCatch.getBody().addStatement(invocReturn);
		}

		if(!tmpref.equals(getFactory().Type().VOID_PRIMITIVE)){
			invocReturn.addTypeCast(variableType.box());
			ret.setReturnedExpression(invocReturn);
		}

		localCatch.getBody().addStatement(ret);
		localCatch.setPosition(methodVar.getPosition());

		CtExecutableReference executableRef = getFactory().Core().createExecutableReference();
		executableRef.setSimpleName("methodEnd");

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setExecutable(executableRef);
		invoc.setTarget(methodAccess);
		invoc.setPosition(methodVar.getPosition());
		CtBlock finalizer = getFactory().Core().createBlock();
		finalizer.addStatement(invoc);
		
		CtTry e = getFactory().Core().createTry();
		e.addCatcher(localCatch);
		e.setFinalizer(finalizer);
		e.setPosition(methodVar.getPosition());

		return e;
	}
	
	protected CtLocalVariable getNewMethodcontext(CtMethod ctMethod) {
		CtTypeReference<?> methodContextRef = getFactory().Type().createReference(MethodContext.class);

		CtExpression methodType;
		if(ctMethod != null && !(ctMethod.getType() instanceof CtTypeParameterReference)) {
			CtTypeReference tmpref = getFactory().Core().clone(ctMethod.getType());
			if(tmpref instanceof CtArrayTypeReference && ((CtArrayTypeReference)tmpref).getComponentType() != null){
				((CtArrayTypeReference)tmpref).getComponentType().getActualTypeArguments().clear();
			}
			methodType = ProcessorUtility.createCtTypeElement(tmpref);
		} else {
			methodType = getFactory().Code().createLiteral(null);
		}
		methodType.setType(getFactory().Type().createReference(Class.class));

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(ctMethod.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(ctMethod.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(ctMethod.getPosition().getSourceEnd());

		CtConstructorCall ctx = getFactory().Code().createConstructorCall(methodContextRef, methodType, lineNumber, sourceStart, sourceEnd);

		List<CtLiteral> args = new ArrayList<>();

		CtLiteral tryNum = getFactory().Code().createLiteral(methodNumber);
		args.add(tryNum);

		return getFactory().Code().createLocalVariable(methodContextRef,
				IConstants.Var.METHODE_CONTEXT + methodNumber,
				ctx);
	}

	protected void collectParams(CtMethod element) {
		List<CtParameter> parameters = element.getParameters();
		for (int i = 0; i < parameters.size(); i++) {
			CtParameter ctParameter = parameters.get(i);


			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

			CtVariableAccess variableRead = getFactory().Code().createVariableRead(ctParameter.getReference(), false);

			CtLiteral<String> variableName = getFactory().Code()
					.createLiteral(variableRead.getVariable().toString());

			CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"varInit",
					variableRead,
					variableName,
					lineNumber,
					sourceStart,
					sourceEnd);
			invoc.setPosition(element.getPosition());

			element.getBody().insertBegin(invoc);
		}
	}

	protected void collectFields(CtMethod element) {
		CtType<?> declaringType = element.getDeclaringType();
		if (declaringType instanceof CtEnum) {
			return;
		}
		if (declaringType.getParent() instanceof CtNewClass) {
			return;
		}
		Collection<CtFieldReference<?>> fields = declaringType.getAllFields();
		for (Iterator<CtFieldReference<?>> iterator = fields.iterator(); iterator.hasNext(); ) {
			CtFieldReference<?> ctFieldReference = iterator.next();
			CtField<?> ctField = ctFieldReference.getDeclaration();
			if (ctField == null) {
				continue;
			}
			if(!ctField.hasModifier(ModifierKind.STATIC) && element.hasModifier(ModifierKind.STATIC)) {
				continue;
			}
			if (ctField.hasModifier(ModifierKind.PUBLIC) || ctField.hasModifier(ModifierKind.PROTECTED)) {

			} else if (ctField.hasModifier(ModifierKind.PRIVATE) && (!element.hasParent(ctField.getParent())
					|| (!ctField.getParent(CtType.class).hasModifier(ModifierKind.STATIC) &&  declaringType.hasModifier(ModifierKind.STATIC)))) {
				continue;
			} else if (!element.getParent(CtPackage.class).equals(ctField.getParent(CtPackage.class))) {
				// default visibility
				continue;
			}

			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

			boolean isStatic = ctField.hasModifier(ModifierKind.STATIC);
			if(ctField.getType() == null) {
				isStatic = true;
			}
			CtFieldAccess variableRead = (CtFieldAccess) getFactory().Code().createVariableRead(ctFieldReference, isStatic);
			if (!isStatic) {
				((CtTypeAccess) ((CtThisAccess) variableRead.getTarget())
						.getTarget())
						.setAccessedType(declaringType.getReference());
			}
			CtLiteral<String> variableName = getFactory().Code()
					.createLiteral(variableRead.getVariable().toString());
			CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(),
					CallChecker.class,
					"varInit",
					variableRead,
					variableName,
					lineNumber,
					sourceStart,
					sourceEnd);
			invoc.setPosition(element.getPosition());
			element.getBody().insertBegin(invoc);
		}
	}

	protected void collectThis(CtMethod element) {
		if(element.hasModifier(ModifierKind.STATIC)) {
			return;
		}
		CtThisAccess thisAccess = getFactory().Code().createThisAccess(element.getDeclaringType().getReference());

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		CtLiteral<String> variableName = getFactory().Code()
				.createLiteral("this");

		CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(),
				CallChecker.class,
				"varInit",
				thisAccess,
				variableName,
				lineNumber,
				sourceStart,
				sourceEnd);
		invoc.setPosition(element.getPosition());
		element.getBody().insertBegin(invoc);
	}
}
