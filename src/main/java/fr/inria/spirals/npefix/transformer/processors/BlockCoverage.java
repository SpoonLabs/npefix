package fr.inria.spirals.npefix.transformer.processors;

import fr.inria.spirals.npefix.resi.CallChecker;
import fr.inria.spirals.npefix.resi.PatchActivationImpl;
import fr.inria.spirals.npefix.resi.context.Location;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtSuperAccess;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class BlockCoverage extends AbstractProcessor<CtBlock> {

	private static int counterInstrumentation;

	private static int counterMethod;

	private static int counterBlock;

	private Date start;

	@Override
	public void init() {
		this.start = new Date();
	}

	@Override
	public void processingDone() {
		System.out.println("BlockCoverage in " + (new Date().getTime() - start.getTime()) + "ms");
		try (FileWriter writer = new FileWriter("instrumentation-block-coverage.txt")) {
			writer.write("counterInstrumentation\tcounterMethod\tcounterBlock" + System
					.getProperty("line.separator"));
			writer.write(counterInstrumentation + "\t" + counterMethod + "\t" + counterBlock);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isToBeProcessed(CtBlock candidate) {
		return super.isToBeProcessed(candidate) && !candidate.isImplicit() && !candidate.getParent(CtType.class).getQualifiedName().contains("org.broadleafcommerce.core.web.processor");
	}

	@Override
	public void process(CtBlock element) {
		CtSwitch aSwitch = element.getParent(CtSwitch.class);
		if (aSwitch != null) {
			// not collect the coverage of alternative patches
			if (aSwitch.getSelector().toString().contains("PatchActivationImpl")) {
				return;
			}
		}

		CtLiteral<Integer> lineNumber = getFactory().Code().createLiteral(element.getPosition().getLine());
		CtLiteral<Integer> sourceStart = getFactory().Code().createLiteral(element.getPosition().getSourceStart());
		CtLiteral<Integer> sourceEnd = getFactory().Code().createLiteral(element.getPosition().getSourceEnd());

		counterInstrumentation++;
		boolean isTypeMember = element.getParent() instanceof CtTypeMember;
		if (isTypeMember) {
			counterMethod++;
		}
		counterBlock++;

		CtInvocation blockCoverage = ProcessorUtility.createStaticCall(getFactory(),
				BlockCoverage.class,
				"aBlock",
				getFactory().createLiteral(isTypeMember),
				lineNumber,
				sourceStart,
				sourceEnd);

		// handle this() and super()
		if (element.getParent() instanceof CtConstructor
				&& element.getStatements().size() > 0 &&
				(element.getStatement(0) instanceof CtThisAccess || element.getStatement(0) instanceof CtSuperAccess)) {
			element.getStatement(0).insertAfter(blockCoverage);
		} else {
			element.insertBegin(blockCoverage);
		}

	}

	static {
		PatchActivationImpl.startRMI();
	}

	public static void aBlock(boolean isExecutableBlock, int line, int sourceStart, int sourceEnd) {
		Location location = CallChecker.getLocation(line, sourceStart, sourceEnd);
		final String output = new Date().getTime() + "\t" + location.getClassName() + "\t" + location.getLine() + "\t" + location.getSourceStart() + "\t" + location.getSourceEnd();
		if (isExecutableBlock) {
			try (FileWriter writer = new FileWriter("method-output.csv", true)) {
				writer.write(output + System.getProperty("line.separator"));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try (FileWriter writer = new FileWriter("block-output.csv", true)) {
			writer.write(output + System.getProperty("line.separator"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
