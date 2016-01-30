package fr.inria.spirals.npefix.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Created by thomas on 24/09/15.
 */
public class RemoveNullCheckProcessor extends AbstractProcessor<CtIf> {

    private boolean isProcessed = false;

    private int count = 0;

    @Override
    public boolean isToBeProcessed(CtIf candidate) {

        if(candidate.getCondition() instanceof CtBinaryOperator) {
            CtBinaryOperator operator = (CtBinaryOperator) candidate.getCondition();
            if(operator.getKind() == BinaryOperatorKind.NE ) {
                if(operator.getLeftHandOperand().toString().equals("null")) {
                    return true;
                }
                if(operator.getRightHandOperand().toString().equals("null")) {
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public void process(CtIf ctIf) {
        count++;
        CtBinaryOperator operator = (CtBinaryOperator) ctIf.getCondition();
        CtStatement body;
        if(operator.getKind() == BinaryOperatorKind.NE) {
            body = ctIf.getThenStatement();
        } else {
            body = ctIf.getElseStatement();
        }
        CtElement parent = ctIf.getParent();
        if(body == null) {
            if(parent instanceof CtStatementList) {
                ((CtStatementList) parent).removeStatement(ctIf);
            }
            return;
        }
        if(body instanceof CtStatementList) {
            List<CtStatement> statements = ((CtStatementList) body).getStatements();
            for (int i = 0; i < statements.size(); i++) {
                CtStatement ctStatement =  statements.get(i);
                if((ctStatement instanceof CtReturn ||
                        ctStatement instanceof CtContinue ||
                        ctStatement instanceof CtBreak ||
                        ctStatement instanceof CtThrow)
                        && !isLastStatementOfMethod(ctStatement)) {
                    CtIf anIf = getFactory().Core().createIf();
                    anIf.setCondition(getFactory().Code().<Boolean>createCodeSnippetExpression("true"));
                    anIf.setThenStatement(ctStatement);
                    ctStatement.setParent(anIf);
                    ctStatement = anIf;
                }
                ctIf.insertBefore(ctStatement);
                ctStatement.setParent(parent);
            }
        } else {
            if((body instanceof CtReturn ||
                    body instanceof CtContinue ||
                    body instanceof CtBreak ||
                    body instanceof CtThrow)
                    && !isLastStatementOfMethod(body)) {
                CtIf anIf = getFactory().Core().createIf();
                anIf.setCondition(getFactory().Code().<Boolean>createCodeSnippetExpression("true"));
                anIf.setThenStatement(body);
                body.setParent(anIf);
                body = anIf;
            }
            ctIf.insertBefore(body);
            body.setParent(parent);
        }
        parent = ctIf.getParent();
        if(parent instanceof CtStatementList) {
            ((CtStatementList) parent).removeStatement(ctIf);
        }
        isProcessed = true;
    }

    private boolean isLastStatementOfMethod(CtStatement statement) {
        CtElement statementParent = statement.getParent();
        if (!(statementParent instanceof CtStatementList)) {
            return isLastStatementOfMethod((CtStatement) statementParent);
        }
        CtStatementList block = (CtStatementList) statementParent;
        if (isLastStatementOf(block, statement)) {
            CtElement blockParent = block.getParent();
            if (blockParent instanceof CtStatement) {
                return isLastStatementOfMethod((CtStatement) blockParent);
            } else {
                return blockParent instanceof CtMethod;
            }
        }
        return false;
    }

    boolean isLastStatementOf(CtStatementList block, CtStatement statement) {
        List<CtStatement> statements = block.getStatements();
        CtStatement lastStatement = statements.get(statements.size() - 1);
        return lastStatement == statement;
    }

    @Override
    public void processingDone() {
        System.out.println("Remove " + count + " if null check");
    }
}
