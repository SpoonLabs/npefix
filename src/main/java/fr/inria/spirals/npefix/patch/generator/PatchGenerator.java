package fr.inria.spirals.npefix.patch.generator;

import fr.inria.spirals.npefix.patch.DecisionElement;
import fr.inria.spirals.npefix.resi.context.Decision;
import fr.inria.spirals.npefix.resi.strategies.Strat1A;
import fr.inria.spirals.npefix.resi.strategies.Strat1B;
import fr.inria.spirals.npefix.resi.strategies.Strat2A;
import fr.inria.spirals.npefix.resi.strategies.Strat2B;
import fr.inria.spirals.npefix.resi.strategies.Strat3;
import fr.inria.spirals.npefix.resi.strategies.Strat4;
import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.LineFilter;

import java.util.List;

class PatchGenerator {
	private List<DecisionElement> decisionElement;
	private Launcher spoon;
	private int[] offset;
	private int[] offsetLine;

	public PatchGenerator(List<DecisionElement> decisionElement, Launcher spoon, int[] offset, int[] offsetLine) {
		this.decisionElement = decisionElement;
		this.spoon = spoon;
		this.offset = offset;
		this.offsetLine = offsetLine;
	}

	private int getOffsetedLine(int line) {
		int output = line;
		for (int i = 0; i < line; i++) {
			output += offsetLine[i];
		}
		return output;
	}

	private int getOffset(int line, int current) {
		int output = current;
		for (int i = 0; i < line; i++) {
			output += offset[i];
		}
		return output;
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
			if (i >= getOffsetedLine(parentLine.getPosition().getLine()) - 1
					&& i <= getOffsetedLine(parentLine.getPosition().getEndLine()) - 1) {
				tmpOffset += s.length() + 1;
				tmpOffsetLine ++;
				if (i == getOffsetedLine(parentLine.getPosition().getLine()) - 1) {
					output.write(patch).line();
				}
			} else {
				output.write(s);
				if (i < split.length - 1) {
					output.line();
				}
			}
		}

		offset[parentLine.getPosition().getLine() - 1] += patch.length() - tmpOffset + 1;
		offsetLine[parentLine.getPosition().getLine() - 1] += patch.split("\n").length - tmpOffsetLine;

		return output.toString();
	}

	private String getPatch(List<DecisionElement> elements) {
		DecisionElement firstDecisionElement = elements.get(0);
		Decision decision = firstDecisionElement.getDecision();
		CtElement firstElement = firstDecisionElement.getElement();
		String classContent = firstDecisionElement.getClassContent();

		String line = getLine(firstDecisionElement);
		CtStatement parentLine = getParentLine(firstElement);
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

		final String indentation = getIndentation(firstDecisionElement);
		Writer writer = new Writer(currentIndentation, indentation);
		if (parentLine.getParent() instanceof CtIf || (parentLine.getParent() instanceof CtBlock
				&& parentLine.getParent().getParent() instanceof CtIf)) {
			writer.write("} else {").tab();
			line = getSubstring(classContent, parentLine);
		}

		Factory factory = firstElement.getFactory();
		factory.getEnvironment().setAutoImports(true);
		String nullElement = getSubstring(classContent, firstElement);

		if (firstElement instanceof CtInvocation && (decision.getStrategy() instanceof Strat2B ||
				decision.getStrategy() instanceof Strat1B)) {
			writer.write(((CtInvocation) firstElement).getType());
			writer.write(" ");
			String variableName = ((CtInvocation) firstElement).getExecutable().getSimpleName();
			variableName = variableName.replace("get", "");
			if (Character.isUpperCase(variableName.charAt(0))) {
				variableName = Character.toLowerCase(variableName.charAt(0)) + variableName.substring(1);
			}
			writer.write(variableName);
			writer.write(" = ");
			writer.write(nullElement);
			writer.write(";").line();
			line = line.replace(nullElement, variableName);
			nullElement = variableName;
		}

		if (parentLine instanceof CtLocalVariable
				&& (decision.getStrategy() instanceof Strat1A
					|| decision.getStrategy() instanceof Strat2A)) {
			int variableNamePosition = line.indexOf(((CtLocalVariable) parentLine).getSimpleName());
			writer.write(line.substring(0, variableNamePosition));
			writer.write(((CtLocalVariable) parentLine).getSimpleName());
			writer.write(";").line();
		}

		writer.write("if (");


		writer.write(nullElement);

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
			writer.write(decision.getInstance().toCtExpression(factory).toString());
			writer.write(";").untab();
			writer.write("}").line();
			writer.write(line);
		} else if (decision.getStrategy() instanceof Strat1B
				|| decision.getStrategy() instanceof Strat2B) {
			writer.write(nullElement);
			writer.write(" = ");
			writer.write(decision.getInstance().toCtExpression(factory).toString());
			writer.write(";").untab();
			writer.write("}").line();
			writer.write(line);
		} else if (decision.getStrategy() instanceof Strat1A
				|| decision.getStrategy() instanceof Strat2A) {
			int sourceStart = getOffset(parentLine.getPosition().getLine(), parentLine.getPosition().getSourceStart());
			for (int j = 0; j < line.length(); j++) {
				char s = line.charAt(j);
				if (s == ' ' || s == '\t') {
					sourceStart--;
					continue;
				}
				break;
			}
			if (parentLine instanceof CtLocalVariable) {
				int variableNamePosition = line.indexOf(((CtLocalVariable) parentLine).getSimpleName());
				line = line.substring(0, getOffset(parentLine.getPosition().getLine(), parentLine.getPosition().getSourceStart()) - sourceStart) + line.substring(variableNamePosition);
				sourceStart += variableNamePosition;
			}

			for (int i = 0; i < elements.size(); i++) {
				DecisionElement element = elements.get(i);
				decision = element.getDecision();
				int start = getOffset(element.getElement().getPosition().getLine(), element.getElement().getPosition().getSourceStart()) - sourceStart;
				int nextStart = line.length();
				if (i < elements.size() - 1) {
					nextStart = getOffset(elements.get(i + 1).getElement().getPosition().getLine(), elements.get(i + 1).getElement().getPosition().getSourceStart()) - sourceStart;
				}
				int end = getOffset(element.getElement().getPosition().getLine(), element.getElement().getPosition().getSourceEnd()) - sourceStart;
				if (i == 0) {
					writer.write(writer.addIndentationToString(line.substring(0, start)));
				}
				writer.write(" ");
				writer.write(decision.getInstance().toCtExpression(factory).toString());
				if (end != nextStart) {
					writer.write(writer.addIndentationToString(line.substring(end + 1, nextStart)));
				}
			}
			writer.untab();

			writer.write("} else {").tab();
			writer.write(writer.addIndentationToString(line)).untab();
			writer.write("}");
		}
		if (parentLine.getParent() instanceof CtIf || (parentLine.getParent() instanceof CtBlock
				&& parentLine.getParent().getParent() instanceof CtIf)) {
			writer.untab().write("}");
		}
		return writer.toString();
	}

	private String getSubstring(String classContent, CtElement element) {
		SourcePosition position = element.getPosition();
		return classContent.substring(
				getOffset(position.getLine(), position.getSourceStart()),
				getOffset(position.getLine(), position.getSourceEnd() + 1));
	}

	private String getIndentation(DecisionElement element) {
		StringBuilder indentation = new StringBuilder();
		CtElement parentLine = element.getElement().getParent(CtMethod.class);
		if (parentLine == null) {
			parentLine = element.getElement().getParent(CtConstructor.class);
		}
		CtElement supParentLine = parentLine.getParent(CtType.class);

		String[] split = element.getClassContent().split("\n");
		String parentFirstLine = split[getOffsetedLine(parentLine.getPosition().getLine()) - 1];
		String supParentFirstLine = split[getOffsetedLine(supParentLine.getPosition().getLine()) - 1];

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

		for (int i = getOffsetedLine(parent.getPosition().getLine()) - 1; i < getOffsetedLine(parent.getPosition().getEndLine()); i++) {
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

	public int[] getOffset() {
		return offset;
	}

	public int[] getOffsetLine() {
		return offsetLine;
	}
}
