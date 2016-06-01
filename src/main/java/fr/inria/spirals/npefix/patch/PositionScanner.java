package fr.inria.spirals.npefix.patch;

import fr.inria.spirals.npefix.resi.context.Location;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

public class PositionScanner extends CtScanner {
	private final Location location;
	private CtElement result;

	public PositionScanner(Location location) {
		this.location = location;
	}

	@Override
	public void scan(CtElement e) {
		if (e == null) {
			return;
		}
		SourcePosition position = e.getPosition();
		if (position == null) {
			return;
		}
		if (position.getLine() == location.getLine() &&
				position.getSourceEnd() == location.getSourceEnd() &&
				position.getSourceStart() == location.getSourceStart()) {
			result = e;
			throw new RuntimeException("Stop");
		} else if (position.getSourceEnd() >= location.getSourceEnd() &&
				position.getSourceStart() <= location.getSourceStart()) {
			super.scan(e);
		}
	}

	public CtElement getResult() {
		return result;
	}
}