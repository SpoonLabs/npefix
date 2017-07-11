package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.Date;
import java.util.List;

import static fr.inria.spirals.npefix.transformer.processors.ProcessorUtility.createCtTypeElement;

/**
 * @author bcornu
 *
 */
@SuppressWarnings("all")
public class ForceNullInit extends AbstractProcessor<CtLocalVariable> {

	private Date start;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("ForceNullInit in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtLocalVariable element) {
		if (element.getDefaultExpression()!= null
				|| element.getParent() instanceof CtForEach)
			return false;
		if (element.hasModifier(ModifierKind.FINAL) && variableInNewClass(element)){
			return false;
		}
		if(element.getParent(CtLambda.class) != null) {
			return false;
		}
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

	private boolean variableInNewClass(final CtLocalVariable e) {
		List<CtVariableRead> elements = e.getParent().getElements(
				new TypeFilter<CtVariableRead>(CtVariableRead.class) {
					@Override
					public boolean matches(CtVariableRead element) {
						CtVariableReference variable = element.getVariable();
						if (variable == null) {
							return false;
						}
						if (e.equals(variable.getDeclaration())) {
							return true;
						}
						return false;
					}
				});
		CtNewClass refParent = e
				.getParent(new TypeFilter<CtNewClass>(CtNewClass.class));
		for (int i = 0; i < elements.size(); i++) {
			CtVariableRead ctVariableRead = elements.get(i);
			CtNewClass parent = ctVariableRead
					.getParent(new TypeFilter<CtNewClass>(CtNewClass.class));
			if (parent != null && parent != refParent) {
				return true;
			}
		}
		return false;
	}
}
