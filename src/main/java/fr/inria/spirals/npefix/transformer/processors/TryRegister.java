package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.transformer.utils.IConstants;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtVariableAccessImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TryRegister extends AbstractProcessor<CtTry> {

	private int tryNumber = 0;
	private CtVariableAccess mainContextVar = null;

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

		mainContextVar = new CtVariableAccessImpl();
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
		CtExecutableReference executableRef = getFactory().Core().createExecutableReference();
		executableRef.setSimpleName("finallyStart");
		
		CtLiteral tryNum = getFactory().Core().createLiteral();
		tryNum.setValue(tryNumber);

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setExecutable(executableRef);
		invoc.setTarget(mainContextVar);
		invoc.setArguments(Arrays.asList(new CtLiteral[]{tryNum}));
		return invoc;
	}

	private CtInvocation getCatchStart(CtLocalVariable tryVar) {
		CtExecutableReference executableRef = getFactory().Core().createExecutableReference();
		executableRef.setSimpleName("catchStart");
		
		CtLiteral tryNum = getFactory().Core().createLiteral();
		tryNum.setValue(tryNumber);

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setExecutable(executableRef);
		invoc.setTarget(mainContextVar);
		invoc.setArguments(Arrays.asList(new CtLiteral[]{tryNum}));
		return invoc;
	}

	private CtLocalVariable getNewTrycontext(CtTry element, List<CtTypeReference> catchables) {
		CtConstructorCall ctx = getFactory().Core().createConstructorCall();
		ctx.setType(getFactory().Type().createReference(IConstants.Class.TRY_CONTEXT));

		List<CtExpression<?>> args = new ArrayList<>();

		CtLiteral tryNum = getFactory().Core().createLiteral();
		tryNum.setValue(tryNumber);
		args.add(tryNum);
		
		CtClass parent = element.getParent(CtClass.class);
		while (parent.isAnonymous() || !parent.isTopLevel()) {
			parent = parent.getParent(CtClass.class);
		}
		args.add(getFactory().Code().createCodeSnippetExpression(parent.getQualifiedName() + ".class"));

		for (CtTypeReference type : catchables) {
			CtLiteral literal = getFactory().Core().createLiteral();
			literal.setValue(type.getQualifiedName());
			args.add(literal);
		}
		ctx.setArguments(args);

		CtLocalVariable context = getFactory().Core().createLocalVariable();
		context.setSimpleName(IConstants.Var.TRY_CONTEXT_PREFIX + tryNumber);
		context.setType(getFactory().Type().createReference(IConstants.Class.TRY_CONTEXT));
		context.setDefaultExpression(ctx);

		return context;
	}

}
