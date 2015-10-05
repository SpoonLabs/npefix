package processors.bcu.transformer.processors;

import processors.bcu.transformer.utils.IConstants;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
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
		CtLocalVariable tryVar = getNewTrycontext(catchables);

		element.insertBefore(tryVar);
		tryVar.setParent(element.getParent());

		mainContextVar = new CtVariableAccessImpl();
		mainContextVar.setVariable(tryVar.getReference());
		
		for (CtCatch catchArg : element.getCatchers()) {
			catchArg.getBody().insertBegin(getCatchStart(tryVar));
		}
		
		if(element.getFinalizer()==null){
			element.setFinalizer(getFactory().Core().createBlock());
		}
		element.getFinalizer().insertBegin(getFinallyStart(tryVar));

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

	private CtLocalVariable getNewTrycontext(List<CtTypeReference> catchables) {
		CtConstructorCall ctx = getFactory().Core().createConstructorCall();
		ctx.setType(getFactory().Type().createReference(IConstants.Class.TRY_CONTEXT));

		List<CtLiteral> args = new ArrayList<>();

		CtLiteral tryNum = getFactory().Core().createLiteral();
		tryNum.setValue(tryNumber);
		args.add(tryNum);

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
