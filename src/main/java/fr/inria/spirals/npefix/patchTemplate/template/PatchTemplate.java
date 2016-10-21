package fr.inria.spirals.npefix.patchTemplate.template;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public interface PatchTemplate {

	/**
	 * Apply a patch template on the null element nullElement.
	 *
	 * @param nullElement the null element to patch
	 * @return the modified element
	 * @throws RuntimeException when the patch cannot be applied
	 */
	CtElement apply(CtExpression nullElement);
}
