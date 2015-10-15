package fr.inria.spirals.npefix.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;

/**
 * Split if condition into several if in order to add check not null before each section of the condition
 */
public class IfSplitter extends AbstractProcessor<CtIf>{
    @Override
    public boolean isToBeProcessed(CtIf candidate) {
        CtExpression<Boolean> condition = candidate.getCondition();
        if(condition instanceof CtBinaryOperator) {
            BinaryOperatorKind kind = ((CtBinaryOperator) condition).getKind();
            return (kind.equals(BinaryOperatorKind.AND)) || kind.equals(BinaryOperatorKind.OR);
        }
        return false;
    }

    @Override
    public void process(CtIf ctIf) {
        CtBinaryOperator condition = (CtBinaryOperator) ctIf.getCondition();
        CtExpression leftHandOperand = condition.getLeftHandOperand();
        CtExpression rightHandOperand = condition.getRightHandOperand();
        BinaryOperatorKind kind = condition.getKind();

        CtIf anIf = getFactory().Core().createIf();
        ctIf.replace(anIf);
        anIf.setParent(ctIf.getParent());
        anIf.setCondition(leftHandOperand);
        ctIf.setCondition(rightHandOperand);
        if(kind.equals(BinaryOperatorKind.AND)) {
            anIf.setThenStatement(wrapBlock(ctIf));
            anIf.setElseStatement(wrapBlock(getFactory().Core().clone(ctIf.getElseStatement())));
        } else {
            anIf.setThenStatement(wrapBlock(getFactory().Core().clone(ctIf.getThenStatement())));
            anIf.setElseStatement(wrapBlock(ctIf));
        }


        if(isToBeProcessed(anIf)) {
            process(anIf);
        }
    }

    private CtStatement wrapBlock(CtStatement element) {
        if(element == null) {
            return null;
        }
        if(element instanceof CtBlock) {
            return element;
        }
        CtBlock<?> ctBlock = getFactory().Code().createCtBlock(element);
        ctBlock.setParent(element.getParent());
        element.setParent(ctBlock);
        return ctBlock;
    }
}
