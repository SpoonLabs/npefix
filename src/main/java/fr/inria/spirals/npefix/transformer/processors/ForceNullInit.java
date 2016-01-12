package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.ModifierKind;

import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.createCtTypeElement;

/**
 * @author bcornu
 *
 */
@SuppressWarnings("all")
public class ForceNullInit extends AbstractProcessor<CtLocalVariable> {

	@Override
	public boolean isToBeProcessed(CtLocalVariable element) {
		if(element.getDefaultExpression()!= null
				|| element.getParent() instanceof CtForEach)
			return false;
		return super.isToBeProcessed(element);
	}

	@Override
	public void process(CtLocalVariable element) {
		if(element.hasModifier(ModifierKind.FINAL)){
			element.removeModifier(ModifierKind.FINAL);
		}

		CtExpression arg = createCtTypeElement(element.getType());
		if(arg == null) {
			return;
		}

		CtInvocation invoc = ProcessorUtility.createStaticCall(getFactory(), CallChecker.class, "init", arg);
		element.setDefaultExpression(invoc);
		invoc.setPosition(element.getPosition());
		invoc.setType(element.getType());
	}

}
