package fr.inria.spirals.npefix.patch;

import fr.inria.spirals.npefix.resi.context.Decision;
import spoon.reflect.declaration.CtElement;

public class DecisionElement {
	private CtElement element;
	private Decision decision;
	private String classContent;

	public DecisionElement(CtElement element, Decision decision) {
		this.element = element;
		this.decision = decision;
	}

	public CtElement getElement() {
		return element;
	}

	public void setElement(CtElement element) {
		this.element = element;
	}

	public Decision getDecision() {
		return decision;
	}

	public void setDecision(Decision decision) {
		this.decision = decision;
	}

	public String getClassContent() {
		return classContent;
	}

	public void setClassContent(String classContent) {
		this.classContent = classContent;
	}
}