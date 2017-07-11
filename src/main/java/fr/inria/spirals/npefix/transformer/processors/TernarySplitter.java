package fr.inria.spirals.npefix.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.Date;
import java.util.List;

/**
 * Split if condition into several if in order to add check not null before each section of the condition
 */
public class TernarySplitter extends AbstractProcessor<CtConditional>{

    private Date start;

    @Override
    public void init() {
        this.start = new Date();
    }

    @Override
    public void processingDone() {
        System.out.println("TernarySplitter  in " + (new Date().getTime() - start.getTime()) + "ms");
    }

    @Override
    public boolean isToBeProcessed(CtConditional candidate) {
        if(candidate.getParent(CtField.class) != null) {
            return false;
        }
        CtStatement parent = candidate.getParent(CtStatement.class);
        if(candidate.getParent(CtConstructor.class) != null && parent instanceof CtInvocation) {
            if(((CtInvocation) parent).getExecutable().getSimpleName().equals("<init>")) {
                return false;
            }
        }
        if(candidate.getParent(CtLambda.class) != null) {
            return false;
        }
        return candidate.getParent() instanceof CtStatement ||true;
    }

    @Override
    public void process(CtConditional ctConditional) {
        CtStatement parent = ctConditional.getParent(CtStatement.class);
        while(!(parent.getParent() instanceof CtStatementList)) {
            parent = parent.getParent(CtStatement.class);
        }
        CtExpression condition = ctConditional.getCondition();

        CtIf anIf = getFactory().Core().createIf();
        anIf.setPosition(ctConditional.getPosition());

        if(parent instanceof CtReturn) {
            if(!((CtReturn) parent).getReturnedExpression().equals(ctConditional)) {
                return;
            }

            CtReturn returnThen = (CtReturn) parent.clone();
            CtReturn returnElse = (CtReturn) parent.clone();

            returnThen.setReturnedExpression(ctConditional.getThenExpression());
            returnElse.setReturnedExpression(ctConditional.getElseExpression());

            List<CtTypeReference> typeCasts = ctConditional.getTypeCasts();
            for (int i = 0; i < typeCasts.size(); i++) {
                CtTypeReference ctTypeReference = typeCasts.get(i);
                returnThen.getReturnedExpression().addTypeCast(ctTypeReference.clone());
                returnElse.getReturnedExpression().addTypeCast(ctTypeReference.clone());
            }

            anIf.setElseStatement(getFactory().Code().createCtBlock(returnElse));
            anIf.setThenStatement(getFactory().Code().createCtBlock(returnThen));
        } else if(parent instanceof CtAssignment) {
            CtAssignment assignment = (CtAssignment) parent;
            CtExpression ctExpression = assignment.getAssignment();
            if(!ctExpression.equals(ctConditional)) {
                if(ctExpression instanceof CtBinaryOperator) {
                    CtBinaryOperator ctBinaryOperator = (CtBinaryOperator) ctExpression;

                    createAssignment(ctConditional, anIf, assignment);
                    CtBinaryOperator cloneThen = ctBinaryOperator.clone();
                    CtBinaryOperator cloneElse = ctBinaryOperator.clone();

                    if(ctBinaryOperator.getLeftHandOperand().equals(ctConditional)) {
                        cloneThen.setLeftHandOperand(ctConditional.getThenExpression());
                        ctConditional.getThenExpression().setParent(cloneThen);
                        cloneElse.setLeftHandOperand(ctConditional.getElseExpression());
                    } else if(ctBinaryOperator.getRightHandOperand().equals(ctConditional)) {
                        cloneThen.setRightHandOperand(ctConditional.getThenExpression());
                        cloneElse.setRightHandOperand(ctConditional.getElseExpression());
                    }

                    cloneThen.getLeftHandOperand().setParent(cloneThen);
                    cloneElse.getLeftHandOperand().setParent(cloneElse);
                    ((CtAssignment)((CtBlock)anIf.getThenStatement()).getStatement(0)).setAssignment(cloneThen);
                    ((CtAssignment)((CtBlock)anIf.getElseStatement()).getStatement(0)).setAssignment(cloneElse);
                } else {
                    return;
                }
            } else {
                createAssignment(ctConditional, anIf, assignment);
            }
        } else if(parent instanceof CtLocalVariable) {
            CtLocalVariable localVariable = (CtLocalVariable) parent;
            if(!localVariable.getDefaultExpression().equals(ctConditional)) {
                return;
            }

            CtLocalVariable clone = localVariable.clone();
            clone.setDefaultExpression(null);

            localVariable.insertBefore(clone);

            CtAssignment variableAssignment = getFactory().Code()
                    .createVariableAssignment(localVariable.getReference(),
                            false, ctConditional);
            variableAssignment.setType(localVariable.getType().clone());
            variableAssignment.setPosition(ctConditional.getPosition());
            createAssignment(ctConditional, anIf, variableAssignment);
        } else if(parent instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) parent;
            CtInvocation cloneThen = invocation.clone();
            CtInvocation cloneElse = invocation.clone();


            List arguments = cloneThen.getArguments();
            boolean found = false;
            for (int i = 0; i < arguments.size(); i++) {
                Object o =  arguments.get(i);
                if(o.equals(ctConditional)) {
                    ctConditional.getThenExpression().setParent(invocation);
                    arguments.set(i, ctConditional.getThenExpression());
                    ctConditional.getElseExpression().setParent(invocation);
                    cloneElse.getArguments().set(i, ctConditional.getElseExpression());
                    found = true;
                    break;
                }
            }
            if(!found) {
                return;
            }

            cloneThen.setParent(anIf);
            cloneElse.setParent(anIf);

            anIf.setElseStatement(getFactory().Code().createCtBlock(cloneElse));
            anIf.setThenStatement(getFactory().Code().createCtBlock(cloneThen));
        } else if(parent instanceof CtConstructorCall) {
            CtConstructorCall invocation = (CtConstructorCall) parent;
            CtConstructorCall cloneThen = invocation.clone();
            CtConstructorCall cloneElse = invocation.clone();

            List arguments = cloneThen.getArguments();
            boolean found = false;
            for (int i = 0; i < arguments.size(); i++) {
                Object o =  arguments.get(i);
                if(o.equals(ctConditional)) {
                    arguments.set(i, ctConditional.getThenExpression());
                    cloneElse.getArguments().set(i, ctConditional.getElseExpression());
                    found = true;
                    break;
                }
            }
            if(!found) {
                return;
            }

            cloneThen.setParent(anIf);
            cloneElse.setParent(anIf);

            anIf.setElseStatement(getFactory().Code().createCtBlock(cloneElse));
            anIf.setThenStatement(getFactory().Code().createCtBlock(cloneThen));
        } else if(parent instanceof CtIf) {
            CtIf elem = (CtIf) parent;
            if(!elem.getCondition().equals(ctConditional)) {
                return;
            }

            CtIf cloneThen = elem.clone();
            cloneThen.setParent(anIf);
            CtIf cloneElse = elem.clone();
            cloneElse.setParent(anIf);

            cloneThen.setCondition(ctConditional.getThenExpression());
            ctConditional.getThenExpression().setParent(cloneThen);

            cloneElse.setCondition(ctConditional.getElseExpression());
            ctConditional.getElseExpression().setParent(cloneElse);

            anIf.setElseStatement(getFactory().Code().createCtBlock(cloneElse));
            anIf.setThenStatement(getFactory().Code().createCtBlock(cloneThen));
        } else if(parent instanceof CtThrow) {
            return;
        } else if(parent instanceof CtLoop) {
            return;
        } else if(parent instanceof CtUnaryOperator) {
            return;
        } else {
            System.err.println(parent);
            throw new RuntimeException("Other " + parent.getClass());
        }
        /*if(ctConditional.getThenExpression().getTypeCasts() == null ||
                ctConditional.getThenExpression().getTypeCasts().isEmpty()) {
            ((CtExpression)anIf.getThenStatement()).setTypeCasts(ctConditional.getTypeCasts());
        }
        if(ctConditional.getElseExpression().getTypeCasts() == null ||
                ctConditional.getElseExpression().getTypeCasts().isEmpty()) {
            ((CtExpression) anIf.getElseStatement())
                    .setTypeCasts(ctConditional.getTypeCasts());
        }*/
        anIf.setCondition(condition);
        condition.setParent(anIf);
        parent.replace(anIf);
    }

    private void createAssignment(CtConditional ctConditional, CtIf anIf,
            CtAssignment assignment) {
        CtAssignment assignmentThen = assignment.clone();
        assignmentThen.setAssignment(ctConditional.getThenExpression());
        assignmentThen.setParent(anIf);
        anIf.setThenStatement(assignmentThen);

        CtAssignment assignmentElse = assignment.clone();
        assignmentElse.setAssignment(ctConditional.getElseExpression());
        assignmentElse.setParent(anIf);
        anIf.setThenStatement(assignmentElse);

        List<CtTypeReference> typeCasts = ctConditional.getTypeCasts();
        for (int i = 0; i < typeCasts.size(); i++) {
            CtTypeReference ctTypeReference = typeCasts.get(i);
            assignmentThen.getAssignment().addTypeCast(ctTypeReference.clone());
            assignmentElse.getAssignment().addTypeCast(ctTypeReference.clone());
        }

        anIf.setElseStatement(getFactory().Code().createCtBlock(assignmentElse));
        anIf.setThenStatement(getFactory().Code().createCtBlock(assignmentThen));
    }
}
