package fr.inria.spirals.npefix.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.reflect.visitor.filter.ReturnOrThrowFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Verify that a variable is not null in a loop or a foreach loop if(varA !=null) for(a in varA)
 */
public class CheckNotNull extends AbstractProcessor<CtBinaryOperator<Boolean>> {

	private static final TypeFilter<CtStatement> lineFilter = new TypeFilter<CtStatement>(
			CtStatement.class) {
		@Override
		public boolean matches(CtStatement element) {
			CtElement parent = element.getParent();
			if (parent instanceof CtStatementList) {
				return true;
			}
			if (parent instanceof CtLoop
					&& ((CtLoop) parent).getBody().equals(element)) {
				return true;
			}
			if (parent instanceof CtIf
					&& (element.equals(((CtIf) parent).getElseStatement())
						|| element.equals(((CtIf) parent).getThenStatement()))){
				return true;
			}

			return super.matches(element);
		}
	};

	@Override
	public boolean isToBeProcessed(CtBinaryOperator<Boolean> element) {
		if(!super.isToBeProcessed(element)) {
			return false;
		}
		BinaryOperatorKind kind = ((CtBinaryOperator) element).getKind();
		if (kind.equals(BinaryOperatorKind.EQ) || kind.equals(BinaryOperatorKind.NE)) {
			return ("null".equals(((CtBinaryOperator) element)
					.getLeftHandOperand().toString()) || "null".equals(((CtBinaryOperator) element)
					.getRightHandOperand().toString()));
		}
		return false;
	}

	@Override
	public void process(CtBinaryOperator<Boolean> element) {
		CtElement notNullElement = element.getLeftHandOperand();
		if (notNullElement.toString().equals("null")) {
			notNullElement = element.getRightHandOperand();
		}
		if (notNullElement instanceof CtVariableAccess<?>) {
			final CtVariableReference variable = ((CtVariableAccess) notNullElement).getVariable();
			CtElement parent = element.getParent(lineFilter);
			List<CtElement> notNullElements = new ArrayList<>();
			if (parent instanceof CtIf) {
				CtStatement thenStatement = ((CtIf) parent).getThenStatement();
				notNullElements.add(((CtIf) parent).getCondition());
				if (element.getKind() == BinaryOperatorKind.EQ) {
					CtStatement elseStatement = ((CtIf) parent).getElseStatement();
					if (elseStatement == null) {
						List<CtCFlowBreak> elements = thenStatement.getElements(new ReturnOrThrowFilter());
						if (!elements.isEmpty()) {
							final CtElement block = parent.getParent();
							if (block != null && block.getPosition() != null) {
								List<CtStatement> postElements = block.getElements(
												new AbstractFilter<CtStatement>() {
													@Override
													public boolean matches(
															CtStatement element) {
														if (element.getPosition() == null) {
															return false;
														}
														return element.getPosition().getLine() > block.getPosition().getEndLine();
													}
												});
								for (CtStatement postElement : postElements) {
									notNullElements.add(postElement);
								}
							}
						}
					} else {
						notNullElements.add(elseStatement);
					}
				} else {
					notNullElements.add(thenStatement);
				}
			} else if (parent instanceof CtConditional) {
				notNullElements.add(((CtConditional) parent).getCondition());
				if (element.getKind() == BinaryOperatorKind.EQ) {
					notNullElements.add(((CtConditional) parent).getElseExpression());
				} else {
					notNullElements.add(((CtConditional) parent).getThenExpression());
				}
			} else if (parent instanceof CtLoop) {
				if (element.getKind() == BinaryOperatorKind.NE) {
					notNullElements.add(((CtLoop) parent).getBody());
				}
			}

			for (int i = 0; i < notNullElements.size(); i++) {
				CtElement ctElement = notNullElements.get(i);
				List<CtVariableAccess> elements = ctElement.getElements(
						new TypeFilter<CtVariableAccess>(CtVariableAccess.class) {
							@Override
							public boolean matches(CtVariableAccess element) {
								return variable.equals(element.getVariable());
							}
						});
				for (CtVariableAccess ctVariableAccess : elements) {
					ctVariableAccess.putMetadata("notnull", true);
				}
			}
		}
	}

}
