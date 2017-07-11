package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.transformer.utils.IConstants;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TryRegister extends AbstractProcessor<CtTry> {

	private Date start;
	private int tryNumber = 0;
	private CtVariableAccess mainContextVar = null;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public boolean isToBeProcessed(CtTry element) {
		if(element.getParent(CtLambda.class) != null) {
			return false;
		}
		return super.isToBeProcessed(element);
	}

	@Override
	public void processingDone() {
		System.out.println("TryRegister # Try: " + tryNumber + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public void process(CtTry element) {
		tryNumber++;
		List<CtTypeReference> catchables = new ArrayList<>();
		for (CtCatch catchArg : element.getCatchers()) {
			catchables.add(catchArg.getParameter().getReference().getType());
		}
		CtLocalVariable tryVar = getNewTrycontext(element, catchables);

		element.insertBefore(tryVar);
		tryVar.setParent(element.getParent());

		mainContextVar = element.getFactory().Core().createVariableRead();
		mainContextVar.setVariable(tryVar.getReference());
		
		for (CtCatch catchArg : element.getCatchers()) {
			CtInvocation catchStart = getCatchStart(tryVar);
			catchArg.getBody().insertBegin(catchStart);
			catchStart.setParent(catchArg.getBody());
		}
		
		if(element.getFinalizer()==null){
			element.setFinalizer(getFactory().Core().createBlock());
		}
		CtStatement finallyStart = getFinallyStart(tryVar);
		element.getFinalizer().insertBegin(finallyStart);
		finallyStart.setParent(element.getFinalizer());

	}

	private CtStatement getFinallyStart(CtLocalVariable tryVar) {
		return createCatch("finallyStart");
	}

	private CtInvocation getCatchStart(CtLocalVariable tryVar) {
		return createCatch("catchStart");
	}

	private CtInvocation createCatch(String name) {
		CtExecutableReference executableRef = getFactory().Core().createExecutableReference();
		executableRef.setSimpleName(name);

		CtLiteral tryNum = getFactory().Core().createLiteral();
		tryNum.setValue(tryNumber);

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setExecutable(executableRef);
		invoc.setTarget(mainContextVar);
		invoc.setArguments(Arrays.asList(new CtLiteral[]{tryNum}));
		return invoc;
	}

	private CtLocalVariable getNewTrycontext(CtTry element, List<CtTypeReference> catchables) {
		CtTypeReference<Object> tryTypeRef = getFactory().Type().createReference(IConstants.Class.TRY_CONTEXT);

		List<CtExpression<?>> args = new ArrayList<>();

		CtLiteral tryNum = getFactory().Code().createLiteral(tryNumber);
		args.add(tryNum);

		CtType parentClass = element.getParent(CtType.class);
		while (parentClass.isAnonymous() || !parentClass.isTopLevel()) {
			parentClass = parentClass.getParent(CtType.class);
		}
		args.add(ProcessorUtility.createCtTypeElement(parentClass.getReference()));
		//args.add(getFactory().Code().createCodeSnippetExpression(parentClass.getQualifiedName() + ".class"));

		for (CtTypeReference type : catchables) {
			args.add(getFactory().Code().createLiteral(type.getQualifiedName()));
		}
		CtConstructorCall ctx = getFactory().Code().createConstructorCall(tryTypeRef, args.toArray(new CtExpression[]{}));
		ctx.setPosition(element.getPosition());

		CtLocalVariable context = getFactory().Code().createLocalVariable(tryTypeRef,
				IConstants.Var.TRY_CONTEXT_PREFIX + tryNumber,
				ctx);
		context.setPosition(element.getPosition());

		return context;
	}

}
