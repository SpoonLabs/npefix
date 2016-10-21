package fr.inria.spirals.npefix.resi.context.instance;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;

public class InstanceFactory {

	public static Instance fromCtExpression(CtExpression expression) {
		Instance instance = null;

		if (expression == null) {
			instance = new PrimitiveInstance(null);
		} else if (expression instanceof CtLiteral) {
			instance = new PrimitiveInstance(((CtLiteral) expression).getValue());
		} else if (expression instanceof CtVariableAccess) {
			CtVariable declaration = ((CtVariableAccess) expression).getVariable().getDeclaration();
			if (declaration != null && declaration.hasModifier(ModifierKind.STATIC)) {
				instance = new StaticVariableInstance(((CtField)declaration).getDeclaringType().getQualifiedName(), declaration.getSimpleName());
			} else {
				instance = new VariableInstance(expression.toString());
			}
		} else if (expression instanceof CtConstructorCall) {
			String classname = (expression).getType().getQualifiedName();
			List<CtTypeReference> parameters = ((CtConstructorCall) expression).getExecutable().getParameters();
			List<CtExpression> constructorArguments = ((CtConstructorCall) expression).getArguments();

			String[] parameterTypes = new String[parameters.size()];
			List<Instance> arguments = new ArrayList<>();

			for (int i = 0; i < parameters.size(); i++) {
				CtTypeReference ctParameter = parameters.get(i);
				parameterTypes[i] = ctParameter.getQualifiedName();

				arguments.add(fromCtExpression(constructorArguments.get(i)));
			}
			instance = new NewInstance(classname, parameterTypes, arguments);
		} else if (expression instanceof CtThisAccess) {
			instance = new VariableInstance(expression.toString());
		} else {
			instance = new PrimitiveInstance(expression.toString());
			System.err.println(expression.getType() + " not handled");
		}
		return instance;
	}

}
