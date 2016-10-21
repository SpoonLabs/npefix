package fr.inria.spirals.npefix.patchTemplate.template;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.LineFilter;

/**
 * Skip the line that contains the null element
 */
public class SkipMethodReturn implements PatchTemplate {

	private CtExpression newInstance;

	public SkipMethodReturn(CtExpression newInstance){
		this.newInstance = newInstance;
	}
	public SkipMethodReturn(){
		this.newInstance = null;
	}

	@Override
	public CtIf apply(CtExpression nullExpression) {
		if (newInstance == null) {
			return applyVoid(nullExpression);
		}
		Factory factory = nullExpression.getFactory();

		CtIf anIf = factory.Core().createIf();


		CtBinaryOperator<Boolean> condition = factory.Code().createBinaryOperator(nullExpression.clone(), factory.Code().createLiteral(null), BinaryOperatorKind.EQ);

		anIf.setCondition(condition);

		CtStatement superLine = nullExpression.getParent(new LineFilter());

		CtTypeMember method = nullExpression.getParent(CtTypeMember.class);
		if (method instanceof CtMethod) {
			// if (nullExpression == null) {return variable;}
			CtReturn aReturn = factory.Core().createReturn();

			aReturn.setReturnedExpression(newInstance);
			anIf.setThenStatement(aReturn);
			superLine.insertBefore(anIf);
		} else {
			throw new RuntimeException("Unsupported patch");
		}
		return anIf;
	}

	private CtIf applyVoid(CtExpression nullExpression) {
		Factory factory = nullExpression.getFactory();

		CtIf anIf = factory.Core().createIf();


		CtBinaryOperator<Boolean> condition = factory.Code().createBinaryOperator(nullExpression.clone(), factory.Code().createLiteral(null), BinaryOperatorKind.EQ);

		anIf.setCondition(condition);

		CtStatement superLine = nullExpression.getParent(new LineFilter());



		CtTypeMember method = nullExpression.getParent(CtTypeMember.class);
		if (method instanceof CtMethod) {
			// if (nullExpression == null) {return;}
			CtReturn aReturn = factory.Core().createReturn();
			if (((CtMethod) method).getType().equals(factory.Type().voidPrimitiveType())) {
				aReturn.setReturnedExpression(null);
			} else if (((CtMethod) method).getType().isPrimitive()) {
				// cannot return null with primitive
				throw new RuntimeException("Unsupported patch");
			} else {
				aReturn.setReturnedExpression(factory.Code().createLiteral(null));
			}
			anIf.setThenStatement(aReturn);
			superLine.insertBefore(anIf);
		} else if (method instanceof CtConstructor) {
			// if (nullExpression != null) {<all next statements>}
			condition.setKind(BinaryOperatorKind.NE);
			CtBlock body = ((CtConstructor) method).getBody();
			int size = body.getStatements().size();
			anIf.setThenStatement(body.clone());
			int index = body.getStatements().indexOf(superLine);
			for (int i = 0; i < index; i++) {
				((CtBlock)anIf.getThenStatement()).removeStatement(body.getStatement(i));
			}
			CtBlock bodyClone = body.clone();
			for (int i = index; i < size; i++) {
				body.removeStatement(bodyClone.getStatement(i));
			}
			if (index > 0) {
				body.getStatement(index - 1).insertAfter(anIf);
			} else {
				body.addStatement(anIf);
			}
		} else {
			throw new RuntimeException("Unsupported patch");
		}
		return anIf;
	}
}
