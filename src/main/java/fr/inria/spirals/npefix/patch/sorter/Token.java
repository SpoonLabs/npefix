package fr.inria.spirals.npefix.patch.sorter;

public interface Token {
	/**
	 * the value of the token, e.g. "{"
	 */
	String getValue();

	/**
	 * the type the token, from JDT
	 */
	int getType();

	/**
	 * returns true if the token is a literal or an identifier (e.g. a class name
	 */
	boolean isText();

	/**
	 * returns true if the token is a literal
	 */
	boolean isLiteral();

	boolean isSyntax();

	boolean isKeyword();

	boolean isOperator();
}
