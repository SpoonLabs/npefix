package fr.inria.spirals.npefix.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtArrayAccess;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
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

	private static final TypeFilter<CtElement> lineFilter = new TypeFilter<CtElement>(CtElement.class) {
		@Override
		public boolean matches(CtElement element) {
			if (!super.matches(element)) {
				return false;
			}
			CtElement parent = element.getParent();
			if (element instanceof CtConditional) {
				return true;
			}
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

			return element instanceof CtStatement;
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
		final CtElement notNullElement;
		if (element.getLeftHandOperand().toString().equals("null")) {
			notNullElement = element.getRightHandOperand();
		} else {
			notNullElement = element.getLeftHandOperand();
		}

		CtElement parent = element.getParent(lineFilter);

		List<CtElement> notNullElements = new ArrayList<>();

		if (parent instanceof CtIf) {
			if(!((CtIf) parent).getCondition().toString().contains(element.toString())) {
				return;
			}
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
									public boolean matches(CtStatement element) {
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
			if(!((CtConditional) parent).getCondition().toString().contains(element.toString())) {
				return;
			}
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
		} else {
			System.out.println(parent);
		}
		for (int i = 0; i < notNullElements.size(); i++) {
			CtElement ctElement = notNullElements.get(i);
			if (ctElement == element) {
				continue;
			}

			List<CtElement> notNulls = getNotNullElements(notNullElement,
					ctElement);
			for (CtElement notNull : notNulls) {
				notNull.putMetadata("notnull", true);
			}
		}

	}

	private List<CtElement> getNotNullElements(final CtElement notNullElement, CtElement ctElement) {
		List<CtElement> notNulls = new ArrayList<>();

		if (notNullElement instanceof CtVariableAccess) {
			final CtVariableReference variable = ((CtVariableAccess) notNullElement).getVariable();
			notNulls.addAll(ctElement.getElements(new AbstractFilter<CtVariableAccess>(CtVariableAccess.class) {
				@Override
				public boolean matches(CtVariableAccess element) {
					if (element == notNullElement) {
						return false;
					}
					return variable.equals(element.getVariable());
				}
			}));
		} else if (notNullElement instanceof CtArrayAccess) {
			notNulls.addAll(ctElement.getElements(new AbstractFilter<CtArrayAccess>(CtArrayAccess.class) {
				@Override
				public boolean matches(CtArrayAccess element) {
					if (element == notNullElement) {
						return false;
					}
					return notNullElement.equals(element);
				}
			}));
		} else if (notNullElement instanceof CtInvocation) {
			notNulls.addAll(ctElement.getElements(new AbstractFilter<CtInvocation>(CtInvocation.class) {
				@Override
				public boolean matches(CtInvocation element) {
					if (element == notNullElement) {
						return false;
					}
					return notNullElement.equals(element);
				}
			}));
		} else if (notNullElement instanceof CtAssignment) {
			return getNotNullElements(((CtAssignment) notNullElement).getAssigned(), ctElement);
		} else {
			System.err.println(notNullElement);
		}
		return notNulls;
	}

}
