package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.context.Location;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtTry;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.LineFilter;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Verify that a variable is not null in a loop or a foreach loop if(varA !=null) for(a in varA)
 */
public class NotNullTracer extends AbstractProcessor<CtBinaryOperator<Boolean>> {

	private static int counterInstrumentation;

	private static int counterCheckNull;

	private static int counterCheckNotNull;

	private Date start;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("NotNullTracer in " + (new Date().getTime() - start.getTime()) + "ms");
		try (FileWriter writer = new FileWriter("instrumentation-counter.txt")) {
			writer.write("counterInstrumentation\tcounterCheckNull\tcounterCheckNotNull" + System.getProperty("line.separator"));
			writer.write(counterInstrumentation + "\t" + counterCheckNull + "\t" + counterCheckNotNull);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isToBeProcessed(CtBinaryOperator<Boolean> element) {
		if(!super.isToBeProcessed(element)) {
			return false;
		}
		if (element.getParent(new TypeFilter<>(CtType.class)).getSimpleName().equals("FileWatcher")) {
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
		CtSwitch aSwitch = element.getParent(CtSwitch.class);
		if (aSwitch != null) {
			// not collect the coverage of alternative patches
			if (aSwitch.getSelector().toString().contains("PatchActivationImpl")) {
				return;
			}
		}
		CtStatement statement = element.getParent(new LineFilter());
		if (!(statement instanceof CtIf)) {
			return;
		}

		counterInstrumentation++;
		if (element.getKind().equals(BinaryOperatorKind.EQ)) {
			counterCheckNull++;
		} else {
			counterCheckNotNull++;
		}

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		CtInvocation ifTracer = ProcessorUtility.createStaticCall(getFactory(),
				NotNullTracer.class,
				"anIf",
				element,
				getFactory().createLiteral(element.toString()),
				lineNumber,
				sourceStart,
				sourceEnd);

		final CtTry aTry = getFactory().createTry();
		aTry.setBody(ifTracer);
		final CtCatch instrumentation_exception = getFactory().createCtCatch("__Instrumentation_Exception", NullPointerException.class, getFactory().createBlock());
		aTry.addCatcher(instrumentation_exception);
		statement.insertBefore(aTry);
	}

	/*static {
		PatchActivationImpl.startRMI();
	}*/

	public static void anIf(boolean value, String expression, int line, int sourceStart, int sourceEnd) {
		Location location = CallChecker.getLocation(line, sourceStart, sourceEnd);
		final String output = new Date().getTime() + "\t" + location.getClassName() + "\t" + location.getLine() + "\t" + location.getSourceStart() + "\t" + location.getSourceEnd() + "\t" + value + "\t" + expression;
		try (FileWriter writer = new FileWriter("instrumentation-output.csv", true)) {
			writer.write(output + System.getProperty("line.separator"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
