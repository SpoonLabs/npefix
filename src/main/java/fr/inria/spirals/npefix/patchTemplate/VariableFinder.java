package fr.inria.spirals.npefix.patchTemplate;

import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtTryWithResource;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtInheritanceScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Find local variables (catch), parameters, fields, super fields
 */
public class VariableFinder {

	private CtElement expression;
	private boolean isStaticContext;

	public VariableFinder(CtElement expression) {
		this.expression = expression;
		CtTypeMember parent = this.expression.getParent(CtTypeMember.class);
		if (parent != null) {
			isStaticContext = parent.hasModifier(ModifierKind.STATIC);
		}
	}

	public List<CtVariable> find() {
		if (expression.isParentInitialized()) {
			return getVariable(expression.getParent());
		}
		return Collections.emptyList();
	}

	public List<CtVariable> find(CtTypeReference type) {
		Set<CtVariable> output = new HashSet<>();
		List<CtVariable> ctVariables = find();

		for (int i = 0; i < ctVariables.size(); i++) {
			CtVariable ctVariable = ctVariables.get(i);
			CtTypeReference variableType = ctVariable.getType();
			if (isAssignableFrom(type, variableType)){
				output.add(ctVariable);
			} else if (variableType instanceof CtArrayTypeReference) {
				variableType = ((CtArrayTypeReference) variableType).getComponentType();
				if (isAssignableFrom(type, variableType)){
					output.add(ctVariable);
				}
			}
		}
		List<CtField> fields = type.getTypeDeclaration().getFields();
		for (int i = 0; i < fields.size(); i++) {
			CtField ctField = fields.get(i);
			if (isAssignableFrom(type, ctField.getType())){
				output.add(ctField);
			}
		}
		return new ArrayList<>(output);
	}

	public List<CtVariable> find(CtType type) {
		return find(type.getReference());
	}

	private List<CtVariable> getVariable(final CtElement parent) {
		final List<CtVariable> variables = new ArrayList<>();
		if (parent == null) {
			return variables;
		}
		class VariableScanner extends CtInheritanceScanner {
			@Override
			public void visitCtStatementList(CtStatementList e) {
				for (int i = 0; i < e.getStatements().size(); i++) {
					CtStatement ctStatement = e.getStatements().get(i);
					if (ctStatement.getPosition() == null) {
						System.out.println(ctStatement);
					}
					if (ctStatement.getPosition() != null
							&& ctStatement.getPosition().getSourceStart() > expression.getPosition().getSourceEnd()) {
						break;
					}
					if (ctStatement instanceof CtVariable) {
						variables.add((CtVariable) ctStatement);
					}
				}
				super.visitCtStatementList(e);
			}

			@Override
			public void visitCtTryWithResource(CtTryWithResource e) {
				variables.addAll(e.getResources());
				super.visitCtTryWithResource(e);
			}

			@Override
			public void scanCtExecutable(CtExecutable e) {
				variables.addAll(e.getParameters());
				super.scanCtExecutable(e);
			}

			@Override
			public <T> void scanCtType(CtType<T> type) {
				List<CtField<?>> fields = type.getFields();
				for (int i = 0; i < fields.size(); i++) {
					CtField<?> ctField = fields.get(i);
					if (isStaticContext) {
						if (!ctField.hasModifier(ModifierKind.STATIC)) {
							continue;
						}
					}
					if (ctField.hasModifier(ModifierKind.PUBLIC) ||
							ctField.hasModifier(ModifierKind.PROTECTED)) {
						variables.add(ctField);
					} else if (ctField.hasModifier(ModifierKind.PRIVATE)) {
						if (expression.hasParent(type)) {
							variables.add(ctField);
						}
					} else if (expression.getParent(CtPackage.class).equals(type.getParent(CtPackage.class))) {
						// default visibility
						variables.add(ctField);
					}
				}
				CtTypeReference<?> superclass = type.getSuperclass();
				if (superclass != null) {
					variables.addAll(getVariable(superclass.getDeclaration()));
				}
				Set<CtTypeReference<?>> superInterfaces = type.getSuperInterfaces();
				for (Iterator<CtTypeReference<?>> iterator = superInterfaces.iterator(); iterator.hasNext(); ) {
					CtTypeReference<?> typeReference = iterator.next();
					variables.addAll(getVariable(typeReference.getDeclaration()));
				}
				super.scanCtType(type);
			}

			@Override
			public void visitCtFor(CtFor e) {
				for (CtStatement ctStatement : e.getForInit()) {
					new VariableScanner().scan(ctStatement);
				}
				super.visitCtFor(e);
			}

			@Override
			public void visitCtForEach(CtForEach e) {
				variables.add(e.getVariable());
				super.visitCtForEach(e);
			}
		}

		new VariableScanner().scan(parent);

		if (parent.isParentInitialized()) {
			variables.addAll(getVariable(parent.getParent()));
		}

		return variables;
	}

	public static boolean  isAssignableFrom(CtTypeReference target, CtTypeReference args) {
		TypeFactory type = target.getFactory().Type();
		// in java all types is assignable to Object
		if (target.equals(type.OBJECT)) {
			return true;
		}
		// handle implicit cast
		if (args.isPrimitive()) {
			if (args.equals(type.BOOLEAN_PRIMITIVE)
					&& target.equals(type.BOOLEAN)) {
				return true;
			}
			if (args.equals(type.INTEGER_PRIMITIVE)
					&& target.equals(type.INTEGER)) {
				return true;
			}
			if (args.equals(type.BYTE_PRIMITIVE)
					&& target.equals(type.BYTE)) {
				return true;
			}
			if (args.equals(type.LONG_PRIMITIVE)
					&& target.equals(type.LONG)) {
				return true;
			}
			if (args.equals(type.FLOAT_PRIMITIVE)
					&& target.equals(type.FLOAT)) {
				return true;
			}
			if (args.equals(type.DOUBLE_PRIMITIVE)
					&& target.equals(type.DOUBLE)) {
				return true;
			}
			if (args.equals(type.CHARACTER_PRIMITIVE)
					&& target.equals(type.CHARACTER)) {
				return true;
			}
			if (args.equals(type.SHORT_PRIMITIVE)
					&& target.equals(type.SHORT)) {
				return true;
			}
			if (args.equals(type.VOID_PRIMITIVE)
					&& target.equals(type.VOID)) {
				return true;
			}
			if (target.equals(type.createReference(Number.class))
					&& !(args.equals(type.VOID_PRIMITIVE)
						|| args.equals(type.CHARACTER_PRIMITIVE)
						|| args.equals(type.BOOLEAN_PRIMITIVE))) {
				return true;
			}
		}
		// if args is a subtype of target
		return args.isSubtypeOf(target);
	}
}
