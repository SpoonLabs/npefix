package fr.inria.spirals.npefix.patchTemplate;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fr.inria.spirals.npefix.patchTemplate.VariableFinder.isAssignableFrom;

/**
 * Find this variable access
 */
public class ThisFinder {

	private final boolean isStaticContext;
	private CtElement expression;

	public ThisFinder(CtElement expression) {
		this.expression = expression;
		CtTypeMember parent = this.expression.getParent(CtTypeMember.class);
		if (parent != null) {
			isStaticContext = parent.hasModifier(ModifierKind.STATIC);
		} else {
			isStaticContext = false;
		}
	}

	public List<CtExpression> find() {
		if (expression.isParentInitialized() && !isStaticContext) {
			return getThis(expression.getParent());
		}
		return Collections.emptyList();
	}

	public List<CtExpression> find(CtTypeReference type) {
		List<CtExpression> output = new ArrayList<>();
		List<CtExpression> ctExpressions = find();

		for (int i = 0; i < ctExpressions.size(); i++) {
			CtExpression ctExpression = ctExpressions.get(i);
			if (isAssignableFrom(type, ctExpression.getType())){
				output.add(ctExpression);
			}
		}
		return output;
	}

	public List<CtExpression> find(CtType type) {
		return find(type.getReference());
	}

	private List<CtExpression> getThis(final CtElement parent) {
		final List<CtExpression> expressions = new ArrayList<>();
		if (parent == null) {
			return expressions;
		}
		class ThisScanner extends CtInheritanceScanner {

			@Override
			public <T> void visitCtClass(CtClass<T> ctClass) {
				expressions.add(ctClass.getFactory().Code().createThisAccess(ctClass.getReference()));
			}
		}

		new ThisScanner().scan(parent);

		if (parent.isParentInitialized()) {
			expressions.addAll(getThis(parent.getParent()));
		}

		return expressions;
	}
}
