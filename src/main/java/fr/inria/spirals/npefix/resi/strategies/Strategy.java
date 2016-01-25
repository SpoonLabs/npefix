package fr.inria.spirals.npefix.resi.strategies;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.Location;

import java.io.Serializable;
import java.util.List;

public interface Strategy extends Comparable<Strategy>, Serializable{

	enum ACTION  {
		// initClass globally a variable
		beforeCalled,
		//
		isCalled,
		// skipLine
		beforeDeref
	}

	boolean isCompatibleAction(ACTION action);

	boolean collectData();

	<T> List<Decision<T>> getSearchSpace(Class<T> clazz, Location location);
}
