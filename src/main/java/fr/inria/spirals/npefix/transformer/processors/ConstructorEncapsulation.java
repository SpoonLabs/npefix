package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.exception.ForceReturn;
import fr.inria.spirals.npefix.transformer.utils.IConstants;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtVariableAccessImpl;
import spoon.support.reflect.code.CtVariableReadImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ajoute les try catch autour des methodes
 * @author bcornu
 *
 */
@SuppressWarnings("all")
public class ConstructorEncapsulation extends AbstractProcessor<CtConstructor> {

	private static int contructor = 0;
	private Date start;

	public static int getCpt(){
		return contructor;
	}

	public ConstructorEncapsulation() {
		contructor = 0;
		this.start = new Date();
	}

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("ConstructorEncapsulation # Constructor: " + contructor + " in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtConstructor ctConstructor) {
		if(ctConstructor.isImplicit()) {
			return false;
		}
		if(ctConstructor.getBody() == null)
			return false;
		contructor++;
		return true;
	}

	@Override
	public void process(CtConstructor ctConstructor) {
		CtLocalVariable methodVar = getNewMethodcontext(ctConstructor);
		methodVar.setPosition(ctConstructor.getPosition());

		CtTry coreTry = createTry(methodVar);
		if(coreTry == null) {
			return;
		}
		boolean isInsered = false;
		coreTry.setBody(getFactory().Core().createBlock());
		List<CtStatement> statements = new ArrayList(ctConstructor.getBody().getStatements());
		for (int i = 0; i < statements.size(); i++) {
			CtStatement ctStatement = statements.get(i);
			String s = ctStatement.toString();
			if (!(s.contains("super(") || s.contains("this("))) {
				ctConstructor.getBody().removeStatement(ctStatement);
				coreTry.getBody().addStatement(ctStatement);
			} else {
				isInsered = true;
				ctStatement.insertAfter(methodVar);
			}
		}
		if (!isInsered) {
			ctConstructor.getBody().addStatement(methodVar);
		}
		methodVar.insertAfter(coreTry);
	}
	
	private CtTry createTry(CtLocalVariable methodVar){
		CtCatchVariable parameter = getFactory().Code().createCatchVariable(getFactory().Type().createReference(ForceReturn.class), "_bcornu_return_t");
		parameter.setPosition(methodVar.getPosition());

		CtVariableAccess methodAccess = getFactory().createVariableRead();
		methodAccess.setVariable(methodVar.getReference());

		CtExecutableReference executableRef = getFactory().Core().createExecutableReference();
		executableRef.setSimpleName("methodEnd");

		CtInvocation invoc = getFactory().Core().createInvocation();
		invoc.setExecutable(executableRef);
		invoc.setTarget(methodAccess);
		invoc.setPosition(methodVar.getPosition());
		CtBlock finalizer = getFactory().Core().createBlock();
		finalizer.addStatement(invoc);
		
		CtTry e = getFactory().Core().createTry();
		e.setFinalizer(finalizer);
		e.setPosition(methodVar.getPosition());

		return e;
	}
	
	private CtLocalVariable getNewMethodcontext(CtConstructor ctConstructor) {
		CtTypeReference<?> methodContextRef = getFactory().Type()
				.createReference(IConstants.Class.METHODE_CONTEXT);

		CtExpression methodType = getFactory().Code().createLiteral(null);
		methodType.setType(getFactory().Type().createReference(Class.class));
		CtConstructorCall ctx = getFactory().Code().createConstructorCall(methodContextRef, methodType);

		List<CtLiteral> args = new ArrayList<>();

		CtLiteral tryNum = getFactory().Code().createLiteral(contructor);
		args.add(tryNum);

		return getFactory().Code().createLocalVariable(methodContextRef,
				IConstants.Var.METHODE_CONTEXT + contructor,
				ctx);
	}
}
