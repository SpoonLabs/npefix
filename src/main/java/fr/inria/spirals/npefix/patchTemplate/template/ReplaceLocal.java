package fr.inria.spirals.npefix.patchTemplate.template;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * Skip the line that contains the null element
 */
public class ReplaceLocal implements PatchTemplate {

	private CtExpression newExpression;

	public ReplaceLocal(CtExpression newExpression) {
		this.newExpression = newExpression;
	}

	public ReplaceLocal(CtClass ctClass, CtExpression...args) {
		this.newExpression = ctClass.getFactory().Code().createConstructorCall(ctClass.getReference(), args);
	}

	public ReplaceLocal(CtVariable variable) {
		this.newExpression = variable.getFactory().Code().createVariableRead(variable.getReference(), variable.hasModifier(ModifierKind.STATIC));
	}

	@Override
	public CtIf apply(CtExpression nullExpression) {
		CtStatement superLine = nullExpression.getParent(new LineFilter()).clone();

		Factory factory = nullExpression.getFactory();

		CtIf anIf = factory.Core().createIf();
		CtBinaryOperator<Boolean> condition = factory.Code().createBinaryOperator(nullExpression.clone(), factory.Code().createLiteral(null), BinaryOperatorKind.EQ);

		anIf.setCondition(condition);

		if (superLine instanceof CtLocalVariable) {
			CtAssignment variableAssignment = factory.Code().createVariableAssignment(((CtLocalVariable) superLine).getReference(), false, null);
			CtAssignment assignmentWhenNull = variableAssignment.clone();
			variableAssignment.setAssignment(((CtLocalVariable) superLine).getDefaultExpression());
			((CtLocalVariable) superLine).setDefaultExpression(null);

			nullExpression.replace(newExpression);

			CtLocalVariable parent = (CtLocalVariable) nullExpression.getParent(new LineFilter());
			assignmentWhenNull.setAssignment(parent.getDefaultExpression().clone());
			anIf.setThenStatement(assignmentWhenNull);
			anIf.setElseStatement(variableAssignment);

			parent.insertBefore(superLine);

			parent.replace(anIf);
		} else {
			nullExpression.replace(newExpression);

			CtStatement parent = nullExpression.getParent(new LineFilter());
			anIf.setThenStatement(parent.clone());
			anIf.setElseStatement(superLine);
			parent.replace(anIf);
		}

		return anIf;
	}
}
