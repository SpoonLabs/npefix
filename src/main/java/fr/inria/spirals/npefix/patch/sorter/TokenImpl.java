package fr.inria.spirals.npefix.patch.sorter;

/**
 * encapsulates a JDT token
 */
public class TokenImpl implements Token {
	final int type;
	final String value;

	public TokenImpl(int type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public boolean isText() {
		return SingleFileTokenIterator.isText(type);
	}

	@Override
	public boolean isLiteral() {
		return SingleFileTokenIterator.isLiteral(type);
	}

	@Override
	public boolean isSyntax() {
		return SingleFileTokenIterator.isSyntax(type);
	}

	@Override
	public boolean isKeyword() {
		return SingleFileTokenIterator.isKeyword(type);
	}

	@Override
	public boolean isOperator() {
		return SingleFileTokenIterator.isOperator(type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TokenImpl token = (TokenImpl) o;

		if (getType() != token.getType())
			return false;
		return getValue() != null ?
				getValue().equals(token.getValue()) :
				token.getValue() == null;

	}

	@Override
	public int hashCode() {
		int result = getType();
		result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return value;
	}
}
