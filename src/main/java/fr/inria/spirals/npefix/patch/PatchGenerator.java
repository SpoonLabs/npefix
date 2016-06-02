package fr.inria.spirals.npefix.patch;

import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.context.instance.PrimitiveInstance;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import spoon.Launcher;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.LineFilter;

import java.util.List;

class PatchGenerator {
	private List<DecisionElement> decisionElement;
	private Launcher spoon;
	private int offset = 0;
	private int offsetLine = 0;

	public PatchGenerator(List<DecisionElement> decisionElement, Launcher spoon, int offset, int offsetLine) {
		this.decisionElement = decisionElement;
		this.spoon = spoon;
		this.offset = offset;
		this.offsetLine = offsetLine;
	}

	public String getPatch() {
		String patch = getPatch(decisionElement);

		CtStatement parentLine = getParentLine(decisionElement.get(0).getElement());
		String classContent = decisionElement.get(0).getClassContent();

		int tmpOffsetLine = 0;
		int tmpOffset = 0;
		String[] split = classContent.split("\n");
		Writer output = new Writer("", "");
		for (int i = 0; i < split.length; i++) {
			String s = split[i];
			if (i >= parentLine.getPosition().getLine() + offsetLine - 1 && i <= parentLine.getPosition().getEndLine() + offsetLine - 1) {
				tmpOffset += s.length();
				tmpOffsetLine ++;
				if (i == parentLine.getPosition().getLine() + offsetLine - 1) {
					output.write(patch).line();
				}
			} else {
				output.write(s);
				if (i < split.length - 1) {
					output.line();
				}
			}
		}

		offset += patch.length() - tmpOffset;
		offsetLine += patch.split("\n").length - tmpOffsetLine;

		return output.toString();
	}

	private String getPatch(List<DecisionElement> elements) {
		Decision decision = elements.get(0).getDecision();

		String line = getLine(elements.get(0));
		String currentIndentation = "";

		for (int i = 0; i < line.length(); i++) {
			char s = line.charAt(i);
			if (s == ' ' || s == '\t') {
				currentIndentation += s;
				continue;
			}
			break;
		}
		line = line.trim();

		final String indentation = getIndentation(elements.get(0));
		Writer writer = new Writer(currentIndentation, indentation);

		writer.write("if (");
		writer.write(elements.get(0).getElement());
		if (decision.getStrategy() instanceof Strat3) {
			writer.write(" != ");
		} else {
			writer.write(" == ");
		}
		writer.write("null) {").tab();
		if (decision.getStrategy() instanceof Strat3) {
			writer.write(writer.addIndentationToString(line)).untab();
			writer.write("}");
		} else if (decision.getStrategy() instanceof Strat4) {
			writer.write("return ");
			switch (((Strat4) decision.getStrategy()).getReturnType()) {
			case VOID:
				break;
			case NULL:
				writer.write("null");
				break;
			case NEW:
				if (!(decision.getInstance() instanceof PrimitiveInstance)) {
					writer.write("new ");
				}
				writer.write(decision.getInstance().toString());
				break;
			case VAR:
				writer.write(decision.getInstance().toString());
				break;
			}
			writer.write(";").untab();
			writer.write("}").line();
			writer.write(line);
		} else if (decision.getStrategy() instanceof Strat1B
				|| decision.getStrategy() instanceof Strat2B) {
			writer.write(elements.get(0).getElement());
			writer.write(" = ");
			if (decision.getStrategy() instanceof Strat2B) {
				if (!(decision.getInstance() instanceof PrimitiveInstance)) {
					writer.write("new ");
				}
			}
			writer.write(decision.getInstance().toString());
			writer.write(";").untab();
			writer.write("}").line();
			writer.write(line);
		} else if (decision.getStrategy() instanceof Strat1A
				|| decision.getStrategy() instanceof Strat2A) {
			CtStatement parentLine = getParentLine(elements.get(0).getElement());
			int sourceStart = parentLine.getPosition().getSourceStart() + offset;
			for (int j = 0; j < line.length(); j++) {
				char s = line.charAt(j);
				if (s == ' ' || s == '\t') {
					sourceStart--;
					continue;
				}
				break;
			}

			for (int i = 0; i < elements.size(); i++) {
				DecisionElement element = elements.get(i);
				decision = element.getDecision();

				int start = element.getElement().getPosition().getSourceStart() - sourceStart + offset;
				int nextStart = line.length();
				if (i < elements.size() - 1) {
					nextStart = elements.get(i + 1).getElement().getPosition().getSourceStart() - sourceStart + offset;
				}
				int end = element.getElement().getPosition().getSourceEnd() - sourceStart + offset;
				if (i == 0) {
					writer.write(writer.addIndentationToString(line.substring(0, start)));
				}
				writer.write(" ");
				if (decision.getStrategy() instanceof Strat2A) {
					if (!(decision.getInstance() instanceof PrimitiveInstance)) {
						writer.write("new ");
					}
				}
				writer.write(decision.getInstance().toString());
				writer.write(writer.addIndentationToString(line.substring(end + 1, nextStart)));
			}
			writer.untab();

			writer.write("} else {").tab();
			writer.write(writer.addIndentationToString(line)).untab();
			writer.write("}");
		}
		return writer.toString();
	}

	private String getIndentation(DecisionElement element) {
		StringBuilder indentation = new StringBuilder();
		CtStatement parentLine = getParentLine(element.getElement());
		CtElement supParentLine = getParentLine(parentLine.getParent());
		if (supParentLine == null) {
			supParentLine = parentLine.getParent(CtMethod.class);
		}
		if (supParentLine == null) {
			supParentLine = parentLine.getParent(CtConstructor.class);
		}

		String[] split = element.getClassContent().split("\n");
		String parentFirstLine = split[parentLine.getPosition().getLine() + offsetLine - 1];
		String supParentFirstLine = split[supParentLine.getPosition().getLine() + offsetLine - 1];

		for (int i = 0; i < parentFirstLine.length(); i++) {
			char s = parentFirstLine.charAt(i);
			if (s == ' ' || s == '\t') {
				indentation.append(s);
				continue;
			}
			break;
		}
		for (int i = 0; i < supParentFirstLine.length(); i++) {
			char s = supParentFirstLine.charAt(i);
			if (s == ' ' || s == '\t') {
				indentation.deleteCharAt(0);
				continue;
			}
			break;
		}

		return indentation.toString();
	}

	private String getLine(DecisionElement element) {
		CtStatement parent = getParentLine(element.getElement());
		String[] split = element.getClassContent().split("\n");
		StringBuilder output = new StringBuilder();

		for (int i = parent.getPosition().getLine() + offsetLine -1; i < parent.getPosition().getEndLine() + offsetLine; i++) {
			String s = split[i];
			output.append(s);
			output.append("\n");
		}

		return output.toString();
	}

	private CtStatement getParentLine(CtElement element) {
		LineFilter lineFilter = new LineFilter();
		if (element instanceof CtStatement) {
			if (lineFilter.matches((CtStatement) element)) {
				return (CtStatement) element;
			}
		}
		return element.getParent(lineFilter);
	}

	public int getOffset() {
		return offset;
	}

	public int getOffsetLine() {
		return offsetLine;
	}
}
