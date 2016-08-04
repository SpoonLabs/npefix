package fr.inria.spirals.npefix.resi.context.instance;

import org.json.JSONObject;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;

import java.io.Serializable;

public interface Instance<T> extends Serializable, Comparable {
	T getValue();

	CtExpression toCtExpression(Factory factory);

	JSONObject toJSON();
}
