package fr.inria.spirals.npefix.patchTemplate;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Find local variables (catch), parameters, fields, super fields
 */
public class InstanceCreator {

	private static Set<CtTypeReference> inCreation = new HashSet<>();

	private final Factory factory;
	private final TypeFactory typeF;
	private CtTypeReference type;

	public InstanceCreator(CtTypeReference type) {
		this.type = type;
		this.factory = type.getFactory();
		this.typeF = this.factory.Type();
	}

	public List<CtExpression> create() {
		List<CtExpression> output = new ArrayList<>();
		if (inCreation.contains(type)) {
			return output;
		}
		if (type.isPrimitive()) {
			return createPrimitive();
		}
		inCreation.add(type);
		if (type.isInterface()) {
			List<CtClass> implementations = getImplementations();
			for (int i = 0; i < implementations.size(); i++) {
				CtClass ctClass = implementations.get(i);
				output.addAll(newInstance(ctClass));
			}
		} else if (type.getTypeDeclaration() instanceof CtEnum) {

		} else if (type.getTypeDeclaration() instanceof CtAnnotation) {

		} else if (type.getTypeDeclaration() instanceof CtClass) {
			output.addAll(newInstance((CtClass) type.getTypeDeclaration()));
		}
		inCreation.remove(type);
		return output;
	}

	private List<CtExpression> newInstance(CtClass ctClass) {
		List<CtExpression> output = new ArrayList<>();
		CtConstructor constructor = ctClass.getConstructor();
		if (constructor != null && constructor.hasModifier(ModifierKind.PUBLIC)) {
			output.add(factory.Code().createConstructorCall(ctClass.getReference()));
		} else {
			Set<CtConstructor> constructors = ctClass.getConstructors();

			constructorLoop:
			for (Iterator<CtConstructor> iterator = constructors.iterator(); iterator.hasNext(); ) {
				CtConstructor ctConstructor = iterator.next();

				List<CtParameter> parameters = ctConstructor.getParameters();
				CtExpression[] values = new CtExpression[parameters.size()];

				for (int j = 0; j < parameters.size(); j++) {
					CtParameter ctParameter = parameters.get(j);
					List<CtExpression> args = new InstanceCreator(ctParameter.getType()).create();
					if (args.isEmpty()) {
						continue constructorLoop;
					}
					values[j] = args.get(0);
				}

				output.add(factory.Code().createConstructorCall(ctClass.getReference(), values));
			}
		}
		return output;
	}

	private List<CtExpression> createPrimitive() {
		List<CtExpression> output = new ArrayList<>();
		if (typeF.BOOLEAN_PRIMITIVE.equals(type)) {
			output.add(factory.Code().createLiteral(true));
			output.add(factory.Code().createLiteral(false));
		} else if (typeF.CHARACTER_PRIMITIVE.equals(type)) {
			output.add(factory.Code().createLiteral(' '));
		} else if (typeF.BYTE_PRIMITIVE.equals(type)) {
			output.add(factory.Code().createLiteral((byte) 0));
		} else {
			output.add(factory.Code().createLiteral(-1));
			output.add(factory.Code().createLiteral(0));
			output.add(factory.Code().createLiteral(1));
		}
		return output;
	}

	private List<CtClass> getImplementations() {
		List<CtClass> output = new ArrayList<>();
		if (type.equals(typeF.createReference(List.class))) {
			output.add(factory.Class().get(ArrayList.class));
			return output;
		} else if (type.equals(typeF.createReference(Set.class))) {
			output.add(factory.Class().get(HashSet.class));
			return output;
		} else if (type.equals(typeF.createReference(Map.class))) {
			output.add(factory.Class().get(HashMap.class));
			return output;
		}
		List<CtType<?>> all = factory.Class().getAll();
		for (int i = 0; i < all.size(); i++) {
			CtType<?> ctType = all.get(i);
			if (!(ctType instanceof CtClass)
					|| ctType.hasModifier(ModifierKind.ABSTRACT)) {
				continue;
			}
			if (ctType.getReference().isSubtypeOf(type)) {
				output.add((CtClass) ctType);
			}
		}
		return output;
	}
}
