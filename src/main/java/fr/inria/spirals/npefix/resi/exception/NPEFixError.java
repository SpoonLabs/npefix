package fr.inria.spirals.npefix.resi.exception;

public class NPEFixError extends Error {

	public NPEFixError(){
		super();
	}

	public NPEFixError(String string) {
		super(string);
	}

	public NPEFixError(String string, Throwable t) {
		super(string, t);
	}

}
