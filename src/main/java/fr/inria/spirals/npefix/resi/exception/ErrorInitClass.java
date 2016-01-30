package fr.inria.spirals.npefix.resi.exception;

/**
 * Created by thomas on 15/10/15.
 */
public class ErrorInitClass extends NPEFixError {
    public ErrorInitClass(){
        super();
    }

    public ErrorInitClass(String string) {
        super(string);
    }
    public ErrorInitClass(String string, Throwable t) {
        super(string, t);
    }
}
