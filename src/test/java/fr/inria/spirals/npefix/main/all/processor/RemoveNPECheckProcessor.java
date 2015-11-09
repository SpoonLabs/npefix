package fr.inria.spirals.npefix.main.all.processor;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

import java.util.List;

/**
 * Created by thomas on 24/09/15.
 */
public class RemoveNPECheckProcessor extends AbstractProcessor<CtCatch> {

    @Override
    public boolean isToBeProcessed(CtCatch candidate) {
        if(candidate.getParameter().getReference().getType().getQualifiedName().equals(NullPointerException.class.getCanonicalName())) {
            return true;
        }
        return false;
    }

    @Override
    public void process(CtCatch candidate) {
        CtTry ctTry = candidate.getParent(CtTry.class);
        CtElement parent = ctTry.getParent();
        ctTry.removeCatcher(candidate);
        if(ctTry.getCatchers().size() == 0) {
            List<CtStatement> statements = ctTry.getBody().getStatements();
            for (int i = 0; i < statements.size(); i++) {
                CtStatement ctStatement = statements.get(i);
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
                ctTry.insertBefore(ctStatement);
                ctStatement.setParent(parent);
            }
            statements = ctTry.getFinalizer().getStatements();
            for (int i = 0; i < statements.size(); i++) {
                CtStatement ctStatement = statements.get(i);
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
                ctTry.insertBefore(ctStatement);
                ctStatement.setParent(parent);
            }
            parent = ctTry.getParent();
            if(parent instanceof CtStatementList) {
                ((CtStatementList) parent).removeStatement(ctTry);
            }
        }
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
}
