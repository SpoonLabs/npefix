package fr.inria.spirals.npefix.patchTemplate.template;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * Skip the line that contains the null element
 */
public class SkipLine implements PatchTemplate {

	@Override
	public CtIf apply(CtExpression nullExpression) {
		Factory factory = nullExpression.getFactory();

		CtIf anIf = factory.Core().createIf();

		// if (nullExpression != null) {}
		CtBinaryOperator<Boolean> condition = factory.Code().createBinaryOperator(nullExpression.clone(), factory.Code().createLiteral(null), BinaryOperatorKind.NE);

		anIf.setCondition(condition);

		CtStatement superLine = nullExpression.getParent(new LineFilter());
		if (superLine instanceof CtFor) {
			throw new RuntimeException("Unsupported patch");
		}
		superLine = extractLocalVariable(superLine);

		anIf.setThenStatement(superLine.clone());
		anIf.getThenStatement().setImplicit(false);

		superLine.replace(anIf);
		return anIf;
	}

	private CtStatement extractLocalVariable(CtStatement statement) {
		if (!(statement instanceof CtLocalVariable)) {
			return statement;
		}
		Factory factory = statement.getFactory();

		CtLocalVariable variable = (CtLocalVariable) statement;

		CtAssignment variableAssignment = factory.Code()
				.createVariableAssignment(variable.getReference(), false,
						variable.getDefaultExpression());

		variable.setDefaultExpression(null);

		statement.insertAfter(variableAssignment);

		return variableAssignment;
	}
}
