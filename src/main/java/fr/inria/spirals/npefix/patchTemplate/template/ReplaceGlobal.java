package fr.inria.spirals.npefix.patchTemplate.template;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * Skip the line that contains the null element
 */
public class ReplaceGlobal implements PatchTemplate {

	private CtExpression newExpression;

	public ReplaceGlobal(CtExpression newExpression) {
		this.newExpression = newExpression;
	}

	public ReplaceGlobal(CtClass ctClass, CtExpression...args) {
		this.newExpression = ctClass.getFactory().Code().createConstructorCall(ctClass.getReference(), args);
	}

	public ReplaceGlobal(CtVariable variable) {
		this.newExpression = variable.getFactory().Code().createVariableRead(variable.getReference(), variable.hasModifier(ModifierKind.STATIC));
	}

	@Override
	public CtIf apply(CtExpression nullExpression) {
		if (!(nullExpression instanceof CtVariableAccess)) {
			return null;
		}
		CtStatement superLine = nullExpression.getParent(new LineFilter());

		Factory factory = nullExpression.getFactory();

		CtIf anIf = factory.Core().createIf();
		CtBinaryOperator<Boolean> condition = factory.Code().createBinaryOperator(nullExpression.clone(), factory.Code().createLiteral(null), BinaryOperatorKind.EQ);

		anIf.setCondition(condition);

		boolean isStatic = ((CtVariableAccess) nullExpression).getVariable().getDeclaration().hasModifier(ModifierKind.STATIC);
		CtAssignment variableAssignment = factory.Code().createVariableAssignment(((CtVariableAccess)nullExpression).getVariable(), isStatic, newExpression);

		anIf.setThenStatement(variableAssignment);

		superLine.insertBefore(anIf);
		return anIf;
	}
}
