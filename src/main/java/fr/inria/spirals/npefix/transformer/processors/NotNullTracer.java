package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Location;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.visitor.filter.LineFilter;

import java.util.Date;

/**
 * Verify that a variable is not null in a loop or a foreach loop if(varA !=null) for(a in varA)
 */
public class NotNullTracer extends AbstractProcessor<CtBinaryOperator<Boolean>> {

	private Date start;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("CheckNotNull  in " + (new Date().getTime() - start.getTime()) + "ms");
	}

	@Override
	public boolean isToBeProcessed(CtBinaryOperator<Boolean> element) {
		if(!super.isToBeProcessed(element)) {
			return false;
		}
		BinaryOperatorKind kind = element.getKind();
		if (kind.equals(BinaryOperatorKind.EQ) || kind.equals(BinaryOperatorKind.NE)) {
			return ("null".equals(((CtBinaryOperator) element).getLeftHandOperand().toString())
					|| "null".equals(((CtBinaryOperator) element).getRightHandOperand().toString()));
		}
		return false;
	}

	@Override
	public void process(CtBinaryOperator<Boolean> element) {
		CtStatement statement = element.getParent(new LineFilter());
		if (!(statement instanceof CtIf)) {
			return;
		}
		CtIf anIf = (CtIf) statement;
		if (((CtBlock)anIf.getThenStatement()).getStatements().size() > 1) {
			return;
		}
		boolean checkNull = element.getKind().equals(BinaryOperatorKind.EQ);

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		CtInvocation ifTracer = ProcessorUtility.createStaticCall(getFactory(),
				NotNullTracer.class,
				"anIf",
				element,
				getFactory().createLiteral(checkNull),
				lineNumber,
				sourceStart,
				sourceEnd);
		anIf.insertBefore(ifTracer);
	}

	public static void anIf(boolean value, boolean checkNull, int line, int sourceStart, int sourceEnd) {
		Location location = CallChecker.getLocation(line, sourceStart, sourceEnd);
		System.out.println(location + " " + (checkNull?"check null":"check not null") + " " + value);
	}
}
