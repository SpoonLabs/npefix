package fr.inria.spirals.npefix.transformer.processors;


import fr.inria.spirals.npefix.main.all.TryCatchRepairStrategy;
import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.transformer.utils.IConstants;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/07/17
 */
@SuppressWarnings("all")
public class TryCatchRepair extends AbstractProcessor<CtMethod> {

	private static int methodNumber = 0;
	private Date start;

	public static int getCpt() {
		return methodNumber;
	}

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("TryCatchRepair # Method: " + methodNumber + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtMethod ctMethode) {
		methodNumber++;
		if (ctMethode.getBody() == null)
			return false;
		if (ctMethode.getType() instanceof CtTypeParameterReference) {
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
		if (tmpref instanceof CtArrayTypeReference && ((CtArrayTypeReference) tmpref).getComponentType() != null) {
			((CtArrayTypeReference) tmpref).getComponentType().getActualTypeArguments().clear();
		}

		CtTry coreTry = createTry(methodVar, tmpref);
		if (coreTry == null) {
			return;
		}
		coreTry.setBody(getFactory().Core().createBlock());
		coreTry.getBody().setStatements(ctMethode.getBody().getStatements());

		List<CtStatement> stats = new ArrayList<CtStatement>();
		stats.add(methodVar);
		stats.add(coreTry);

		ctMethode.getBody().setStatements(stats);
	}

	private CtTry createTry(CtLocalVariable methodVar, CtTypeReference tmpref) {
		if ((tmpref instanceof CtTypeParameterReference)) {
			return null;
		}

		CtCatchVariable parameter = getFactory().Code().createCatchVariable(getFactory().Type().createReference(Throwable.class), "_bcornu_return_t");
		parameter.setPosition(methodVar.getPosition());

		CtCatch localCatch = getFactory().Core().createCatch();
		localCatch.setParameter(parameter);
		localCatch.setBody(getFactory().Core().createBlock());

		CtVariableAccess methodAccess = getFactory().createVariableRead();
		methodAccess.setVariable(methodVar.getReference());

		CtReturn ret = getFactory().Core().createReturn();
		ret.setPosition(methodVar.getPosition());

		CtExpression arg = ProcessorUtility.createCtTypeElement(tmpref);

		final CtBinaryOperator equalsLine = getFactory().createBinaryOperator();

		final CtInvocation invocation = getFactory().createInvocation();
		final CtExecutableReference getTargetLine = getFactory().Core().createExecutableReference();
		getTargetLine.setStatic(true);
		getTargetLine.setType(getFactory().Type().integerPrimitiveType());
		getTargetLine.setDeclaringType(getFactory().createCtTypeReference(TryCatchRepairStrategy.class));
		getTargetLine.setSimpleName("getTargetLine");
		invocation.setExecutable(getTargetLine);
		invocation.setTarget(getFactory().createTypeAccess(getFactory().createCtTypeReference(TryCatchRepairStrategy.class)));

		equalsLine.setKind(BinaryOperatorKind.EQ);
		equalsLine.setLeftHandOperand(invocation);
		equalsLine.setRightHandOperand(getFactory().createLiteral(methodVar.getPosition().getLine()));

		final CtIf ctIf = getFactory().createIf();
		ctIf.setCondition(equalsLine);

		final CtThrow aThrow = getFactory().createThrow();
		aThrow.setThrownExpression(getFactory().createVariableRead(parameter.getReference(), false));
		ctIf.setElseStatement(aThrow);

		final CtInvocation<Decision> invokeGetDecision = getFactory().createInvocation();
		final CtExecutableReference<Decision> getDecision = getFactory().createExecutableReference();
		getDecision.setStatic(true);
		getDecision.setType(getFactory().<Decision>createCtTypeReference(Decision.class));
		getDecision.setDeclaringType(getFactory().createCtTypeReference(TryCatchRepairStrategy.class));
		getDecision.setSimpleName("getDecision");

		getDecision.setParameters(Arrays.asList(
				getFactory().createCtTypeReference(Class.class),
				getFactory().Type().STRING,
				getFactory().Type().INTEGER_PRIMITIVE,
				getFactory().Type().INTEGER_PRIMITIVE,
				getFactory().Type().INTEGER_PRIMITIVE
		));

		invokeGetDecision.setArguments(Arrays.asList(
				getFactory().createCodeSnippetExpression("getClass()"),
				getFactory().createCodeSnippetExpression("getClass().toString()"),
				getFactory().Code().createLiteral(methodVar.getPosition().getLine()),
				getFactory().Code().createLiteral(methodVar.getPosition().getSourceStart()),
				getFactory().Code().createLiteral(methodVar.getPosition().getSourceEnd())
		));

		invokeGetDecision.setExecutable(getDecision);
		invokeGetDecision.setTarget(getFactory().createTypeAccess(getFactory().createCtTypeReference(TryCatchRepairStrategy.class)));

		final CtInvocation<Object> invokeGetValue = getFactory().createInvocation();
		final CtExecutableReference<Object> getValue = getFactory().createExecutableReference();
		getValue.setDeclaringType(getFactory().createCtTypeReference(Decision.class));
		getValue.setType(getFactory().createCtTypeReference(Object.class));
		getValue.setSimpleName("getValue");
		invokeGetValue.setExecutable(getValue);
		invokeGetValue.setTarget(invokeGetDecision);

		CtTypeReference variableType = tmpref;
		if(tmpref.equals(getFactory().Type().VOID_PRIMITIVE)){
			variableType = getFactory().Code().createCtTypeReference(Object.class);
			localCatch.getBody().addStatement(invokeGetValue);
		}

		if(!tmpref.equals(getFactory().Type().VOID_PRIMITIVE)){
			invokeGetValue.addTypeCast(variableType.box());
			ret.setReturnedExpression(invokeGetValue);
		}

		ret.setReturnedExpression(invokeGetValue);

		ctIf.setThenStatement(ret);

		localCatch.getBody().addStatement(ctIf);
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

	private CtLocalVariable getNewMethodcontext(CtMethod ctMethod) {
		CtTypeReference<?> methodContextRef = getFactory().Type()
				.createReference(IConstants.Class.METHODE_CONTEXT);

		CtExpression methodType;
		if (ctMethod != null && !(ctMethod.getType() instanceof CtTypeParameterReference)) {
			CtTypeReference tmpref = getFactory().Core().clone(ctMethod.getType());
			if (tmpref instanceof CtArrayTypeReference && ((CtArrayTypeReference) tmpref).getComponentType() != null) {
				((CtArrayTypeReference) tmpref).getComponentType().getActualTypeArguments().clear();
			}
			methodType = ProcessorUtility.createCtTypeElement(tmpref);
		} else {
			methodType = getFactory().Code().createLiteral(null);
		}
		methodType.setType(getFactory().Type().createReference(Class.class));
		CtConstructorCall ctx = getFactory().Code().createConstructorCall(methodContextRef, methodType);

		List<CtLiteral> args = new ArrayList<>();

		CtLiteral tryNum = getFactory().Code().createLiteral(methodNumber);
		args.add(tryNum);

		return getFactory().Code().createLocalVariable(methodContextRef,
				IConstants.Var.METHODE_CONTEXT + methodNumber,
				ctx);
	}

	private void collectParams(CtMethod element) {
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

	private void collectFields(CtMethod element) {
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
			if (!ctField.hasModifier(ModifierKind.STATIC) && element.hasModifier(ModifierKind.STATIC)) {
				continue;
			}
			if (ctField.hasModifier(ModifierKind.PUBLIC) || ctField.hasModifier(ModifierKind.PROTECTED)) {

			} else if (ctField.hasModifier(ModifierKind.PRIVATE) && (!element.hasParent(ctField.getParent())
					|| (!ctField.getParent(CtType.class).hasModifier(ModifierKind.STATIC) && declaringType.hasModifier(ModifierKind.STATIC)))) {
				continue;
			} else if (!element.getParent(CtPackage.class).equals(ctField.getParent(CtPackage.class))) {
				// default visibility
				continue;
			}

			CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
			CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
			CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

			boolean isStatic = ctField.hasModifier(ModifierKind.STATIC);
			if (ctField.getType() == null) {
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

	private void collectThis(CtMethod element) {
		if (element.hasModifier(ModifierKind.STATIC)) {
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
