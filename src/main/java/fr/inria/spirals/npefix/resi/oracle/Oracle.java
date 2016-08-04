package fr.inria.spirals.npefix.resi.oracle;

import org.json.JSONObject;

import java.io.Serializable;

public interface Oracle extends Serializable {
	boolean isValid();

	String getError();

	JSONObject toJSON();
}
