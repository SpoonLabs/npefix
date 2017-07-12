package fr.inria.spirals.npefix.resi.context.instance;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.exception.ForceReturn;
import org.json.JSONObject;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/07/17
 */
public class ThrowExceptionInstance<T> extends AbstractInstance<T> {

	private Decision<T> decision;

	public void setDecision(Decision<T> decision) {
		this.decision = decision;
	}

	@Override
	public T getValue() {
		throw new ForceReturn(this.decision);
	}

	@Override
	public CtExpression toCtExpression(Factory factory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public JSONObject toJSON() {
		return null;
	}
}
