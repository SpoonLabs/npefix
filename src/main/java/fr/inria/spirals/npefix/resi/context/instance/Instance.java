package fr.inria.spirals.npefix.resi.context.instance;

import java.io.Serializable;

public interface Instance<T> extends Serializable {
	T getValue();
}
