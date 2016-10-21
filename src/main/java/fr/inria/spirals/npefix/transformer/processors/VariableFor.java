package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Date;

/**
 * Verify that a variable is not null in a loop or a foreach loop if(varA !=null) for(a in varA)
 */
public class VariableFor extends AbstractProcessor<CtVariableRead<?>> {

	private Date start;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("VariableFor in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtVariableRead<?> element) {
		CtElement parent = element.getParent();
		if(!(parent instanceof CtForEach
				|| parent instanceof CtFor))
			return false;

		if(element.getVariable().getDeclaration() == null) {
			System.out.println(element.getVariable().getDeclaration());
			return false;
		}

		// for variable declared in a for and used oin the declaration ex: for(boolean loop = true;loop;)
		if(element.getVariable().getDeclaration().getParent().equals(element.getParent())) {
			return false;
		}
		if (element.getMetadata("notnull") != null) {
			return false;
		}
		return true;
	}

	@Override
	public void process(CtVariableRead<?> element) {
		CtElement parent = element.getParent();

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		CtMethod ctMethod = element.getParent(CtMethod.class);

		CtExpression methodType;
		if(ctMethod != null) {
			CtTypeReference tmpref = getFactory().Core().clone(ctMethod.getType());
			if(tmpref instanceof CtArrayTypeReference
					&& ((CtArrayTypeReference)tmpref).getComponentType() != null){
				((CtArrayTypeReference)tmpref).getComponentType().getActualTypeArguments().clear();
			}
			methodType = ProcessorUtility.createCtTypeElement(tmpref);
		} else {
			methodType = getFactory().Code().createLiteral(null);
		}

		CtInvocation ifInvoc = ProcessorUtility.createStaticCall(getFactory(),
				CallChecker.class,
				"beforeDeref",
				element,
				methodType,
				lineNumber,
				sourceStart,
				sourceEnd);
		ifInvoc.setPosition(element.getPosition());

		CtIf encaps = getFactory().Core().createIf();
		encaps.setCondition(ifInvoc);
		encaps.setPosition(element.getPosition());

		CtBlock thenBloc = getFactory().Core().createBlock();

		((CtStatement) parent).replace(encaps);
		encaps.setThenStatement(thenBloc);
		thenBloc.addStatement((CtStatement) parent);
	}

}
