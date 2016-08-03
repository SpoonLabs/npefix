package fr.inria.spirals.npefix.patch.sorter;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.PublicScanner;

import java.util.Iterator;

/**
 * iterates over the probabilityPatch of size n in a given file
 */
public class StringTokenIterator implements Iterator<Token> {
	String fText;
	PublicScanner scanner;
	int currentTokenType;
	int nextTokenType;
	int ngrams;

	public StringTokenIterator(String f, int n) {
		fText = f;
		long complianceLevelValue = CompilerOptions.versionToJdkLevel("1.8");
		scanner = new PublicScanner(false, false, false, 3145728L, complianceLevelValue, (char[][])null, (char[][])null, true);
		scanner.sourceLevel = complianceLevelValue;
		scanner.setSource(fText.toCharArray());
		ngrams = n;
		getNextLowLevelToken();
	}

	static boolean isString(int tokenType) {
		return tokenType == ITerminalSymbols.TokenNameStringLiteral;
	}

	public static boolean isSyntax(int tokenType) {
		return tokenType == ITerminalSymbols.TokenNameLPAREN
				|| tokenType == ITerminalSymbols.TokenNameRPAREN
				|| tokenType == ITerminalSymbols.TokenNameLBRACE
				|| tokenType == ITerminalSymbols.TokenNameRBRACE
				|| tokenType == ITerminalSymbols.TokenNameDOT
				|| tokenType == ITerminalSymbols.TokenNameCOLON
				|| tokenType == ITerminalSymbols.TokenNameCOMMA
				|| tokenType == ITerminalSymbols.TokenNameSEMICOLON
				|| tokenType == ITerminalSymbols.TokenNameLBRACKET
				|| tokenType == ITerminalSymbols.TokenNameRBRACKET
				|| tokenType == ITerminalSymbols.TokenNameCOLON_COLON;
	}

	public static boolean isOperator(int tokenType) {
		return tokenType == ITerminalSymbols.TokenNamePLUS_PLUS
				|| tokenType == ITerminalSymbols.TokenNameMINUS_MINUS
				|| tokenType == ITerminalSymbols.TokenNameEQUAL_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameLESS_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameGREATER_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameNOT_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameLEFT_SHIFT
				|| tokenType == ITerminalSymbols.TokenNameRIGHT_SHIFT
				|| tokenType == ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT
				|| tokenType == ITerminalSymbols.TokenNamePLUS_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameMINUS_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameMULTIPLY_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameDIVIDE_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameAND_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameOR_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameXOR_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameREMAINDER_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameLEFT_SHIFT_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameRIGHT_SHIFT_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL
				|| tokenType == ITerminalSymbols.TokenNameOR_OR
				|| tokenType == ITerminalSymbols.TokenNameAND_AND
				|| tokenType == ITerminalSymbols.TokenNamePLUS
				|| tokenType == ITerminalSymbols.TokenNameMINUS
				|| tokenType == ITerminalSymbols.TokenNameNOT
				|| tokenType == ITerminalSymbols.TokenNameREMAINDER
				|| tokenType == ITerminalSymbols.TokenNameXOR
				|| tokenType == ITerminalSymbols.TokenNameAND
				|| tokenType == ITerminalSymbols.TokenNameMULTIPLY
				|| tokenType == ITerminalSymbols.TokenNameOR
				|| tokenType == ITerminalSymbols.TokenNameTWIDDLE
				|| tokenType == ITerminalSymbols.TokenNameDIVIDE
				|| tokenType == ITerminalSymbols.TokenNameGREATER
				|| tokenType == ITerminalSymbols.TokenNameLESS
				|| tokenType == ITerminalSymbols.TokenNameEQUAL;
	}

	public static boolean isKeyword(int tokenType) {
		return tokenType == ITerminalSymbols.TokenNameabstract
				|| tokenType == ITerminalSymbols.TokenNameassert
				|| tokenType == ITerminalSymbols.TokenNameboolean
				|| tokenType == ITerminalSymbols.TokenNamebreak
				|| tokenType == ITerminalSymbols.TokenNamebyte
				|| tokenType == ITerminalSymbols.TokenNamecase
				|| tokenType == ITerminalSymbols.TokenNamecatch
				|| tokenType == ITerminalSymbols.TokenNamechar
				|| tokenType == ITerminalSymbols.TokenNameclass
				|| tokenType == ITerminalSymbols.TokenNamecontinue
				|| tokenType == ITerminalSymbols.TokenNamedefault
				|| tokenType == ITerminalSymbols.TokenNamedo
				|| tokenType == ITerminalSymbols.TokenNamedouble
				|| tokenType == ITerminalSymbols.TokenNameelse
				|| tokenType == ITerminalSymbols.TokenNameextends
				|| tokenType == ITerminalSymbols.TokenNamefalse
				|| tokenType == ITerminalSymbols.TokenNamefinal
				|| tokenType == ITerminalSymbols.TokenNamefinally
				|| tokenType == ITerminalSymbols.TokenNamefloat
				|| tokenType == ITerminalSymbols.TokenNamefor
				|| tokenType == ITerminalSymbols.TokenNameif
				|| tokenType == ITerminalSymbols.TokenNameimplements
				|| tokenType == ITerminalSymbols.TokenNameimport
				|| tokenType == ITerminalSymbols.TokenNameinstanceof
				|| tokenType == ITerminalSymbols.TokenNameint
				|| tokenType == ITerminalSymbols.TokenNameinterface
				|| tokenType == ITerminalSymbols.TokenNamelong
				|| tokenType == ITerminalSymbols.TokenNamenative
				|| tokenType == ITerminalSymbols.TokenNamenew
				|| tokenType == ITerminalSymbols.TokenNamenull
				|| tokenType == ITerminalSymbols.TokenNamepackage
				|| tokenType == ITerminalSymbols.TokenNameprivate
				|| tokenType == ITerminalSymbols.TokenNameprotected
				|| tokenType == ITerminalSymbols.TokenNamepublic
				|| tokenType == ITerminalSymbols.TokenNamereturn
				|| tokenType == ITerminalSymbols.TokenNameshort
				|| tokenType == ITerminalSymbols.TokenNamestatic
				|| tokenType == ITerminalSymbols.TokenNamestrictfp
				|| tokenType == ITerminalSymbols.TokenNamesuper
				|| tokenType == ITerminalSymbols.TokenNameswitch
				|| tokenType == ITerminalSymbols.TokenNamesynchronized
				|| tokenType == ITerminalSymbols.TokenNamethis
				|| tokenType == ITerminalSymbols.TokenNamethrow
				|| tokenType == ITerminalSymbols.TokenNamethrows
				|| tokenType == ITerminalSymbols.TokenNametransient
				|| tokenType == ITerminalSymbols.TokenNametry
				|| tokenType == ITerminalSymbols.TokenNamevoid
				|| tokenType == ITerminalSymbols.TokenNamevolatile
				|| tokenType == ITerminalSymbols.TokenNamewhile
				|| tokenType == ITerminalSymbols.TokenNameLPAREN
				|| tokenType == ITerminalSymbols.TokenNameRPAREN
				|| tokenType == ITerminalSymbols.TokenNameLBRACE
				|| tokenType == ITerminalSymbols.TokenNameRBRACE
				|| tokenType == ITerminalSymbols.TokenNameLBRACKET
				|| tokenType == ITerminalSymbols.TokenNameRBRACKET
				|| tokenType == ITerminalSymbols.TokenNameSEMICOLON
				|| tokenType == ITerminalSymbols.TokenNameQUESTION
				|| tokenType == ITerminalSymbols.TokenNameenum
				|| tokenType == ITerminalSymbols.TokenNameAT
				|| tokenType == ITerminalSymbols.TokenNameconst
				|| tokenType == ITerminalSymbols.TokenNamegoto
				|| tokenType == ITerminalSymbols.TokenNameARROW;
	}

	static boolean isLiteral(int tokenType) {
		return tokenType == ITerminalSymbols.TokenNameStringLiteral
				|| tokenType == ITerminalSymbols.TokenNameIntegerLiteral
				|| tokenType == ITerminalSymbols.TokenNameLongLiteral
				|| tokenType == ITerminalSymbols.TokenNameFloatingPointLiteral
				|| tokenType == ITerminalSymbols.TokenNameDoubleLiteral
				|| tokenType == ITerminalSymbols.TokenNameCharacterLiteral
				|| tokenType == ITerminalSymbols.TokenNametrue
				|| tokenType == ITerminalSymbols.TokenNamefalse;
	}

	static boolean isText(int tokenType) {
		return isLiteral(tokenType) || isString(tokenType);
	}

	@Override
	public boolean hasNext() {
		return nextTokenType != ITerminalSymbols.TokenNameEOF;
	}

	@Override
	public Token next() {
		if (!hasNext()) {
			throw new RuntimeException();
		}

		currentTokenType = nextTokenType;
		int start = scanner.getCurrentTokenStartPosition();
		int end = scanner.getCurrentTokenEndPosition() + 1;
		// scanner.get

		// todo if this is a new line
		// reset the stack

		Token token = null;
		if (currentTokenType != ITerminalSymbols.TokenNameEOF) {
			String s = fText.substring(start, end);
			token = new TokenImpl(currentTokenType, "#" + s);
		}


		getNextLowLevelToken();
		return token;
	}

	public void getNextLowLevelToken() {
		try {
			nextTokenType = scanner.getNextToken();
			while (!hasNext()
					&& nextTokenType != ITerminalSymbols.TokenNameEOF) {
				nextTokenType = scanner.getNextToken();
			}
		} catch (InvalidInputException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
