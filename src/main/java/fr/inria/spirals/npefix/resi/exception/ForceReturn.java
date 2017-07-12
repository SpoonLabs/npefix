package fr.inria.spirals.npefix.resi.exception;

import fr.inria.spirals.npefix.resi.context.Decision;

public class ForceReturn extends RuntimeException {
	private Decision decision;

	public ForceReturn(Decision decision) {
		this.decision = decision;
	}

	public Decision getDecision() {
		return decision;
	}
}
