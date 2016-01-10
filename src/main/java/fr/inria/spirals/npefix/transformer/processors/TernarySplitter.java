package fr.inria.spirals.npefix.transformer.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;

import java.util.List;

/**
 * Split if condition into several if in order to add check not null before each section of the condition
 */
public class TernarySplitter extends AbstractProcessor<CtConditional>{

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

            CtReturn<Object> returnThen = getFactory().Core().clone(((CtReturn) parent));
            CtReturn<Object> returnElse = getFactory().Core().clone(((CtReturn) parent));

            returnThen.setReturnedExpression(ctConditional.getThenExpression());
            returnElse.setReturnedExpression(ctConditional.getElseExpression());

            returnThen.getReturnedExpression().setTypeCasts(ctConditional.getThenExpression().getTypeCasts());
            returnElse.getReturnedExpression().setTypeCasts(ctConditional.getElseExpression().getTypeCasts());

            anIf.setElseStatement(returnElse);
            anIf.setThenStatement(returnThen);
        } else if(parent instanceof CtAssignment) {
            CtAssignment assignment = (CtAssignment) parent;
            CtExpression ctExpression = assignment.getAssignment();
            if(!ctExpression.equals(ctConditional)) {
                if(ctExpression instanceof CtBinaryOperator) {
                    CtBinaryOperator ctBinaryOperator = (CtBinaryOperator) ctExpression;

                    createAssignment(ctConditional, anIf, assignment);
                    CtBinaryOperator cloneThen = getFactory().Core().clone(ctBinaryOperator);
                    CtBinaryOperator cloneElse = getFactory().Core().clone(ctBinaryOperator);

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
                    ((CtAssignment)anIf.getThenStatement()).setAssignment(cloneThen);
                    ((CtAssignment)anIf.getElseStatement()).setAssignment(cloneElse);
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

            CtLocalVariable clone = getFactory().Core().clone(localVariable);
            clone.setDefaultExpression(null);

            localVariable.insertBefore(clone);

            CtAssignment variableAssignment = getFactory().Code()
                    .createVariableAssignment(localVariable.getReference(),
                            false, ctConditional);
            variableAssignment.setPosition(ctConditional.getPosition());
            createAssignment(ctConditional, anIf,
                    variableAssignment);
        } else if(parent instanceof CtInvocation) {
            CtInvocation invocation = (CtInvocation) parent;
            CtInvocation cloneThen = getFactory().Core().clone(invocation);
            CtInvocation cloneElse = getFactory().Core().clone(invocation);


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

            cloneThen.setTypeCasts(ctConditional.getThenExpression().getTypeCasts());
            cloneElse.setTypeCasts(ctConditional.getElseExpression().getTypeCasts());

            anIf.setThenStatement(cloneThen);
            anIf.setElseStatement(cloneElse);
        } else if(parent instanceof CtConstructorCall) {
            CtConstructorCall invocation = (CtConstructorCall) parent;
            CtConstructorCall cloneThen = getFactory().Core().clone(invocation);
            CtConstructorCall cloneElse = getFactory().Core().clone(invocation);

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

            cloneThen.setTypeCasts(ctConditional.getThenExpression().getTypeCasts());
            cloneElse.setTypeCasts(ctConditional.getElseExpression().getTypeCasts());

            anIf.setThenStatement(cloneThen);
            anIf.setElseStatement(cloneElse);
        } else if(parent instanceof CtIf) {
            CtIf elem = (CtIf) parent;
            if(!elem.getCondition().equals(ctConditional)) {
                return;
            }

            CtIf cloneThen = getFactory().Core().clone(elem);
            cloneThen.setParent(anIf);
            CtIf cloneElse = getFactory().Core().clone(elem);
            cloneElse.setParent(anIf);

            cloneThen.setCondition(ctConditional.getThenExpression());
            ctConditional.getThenExpression().setParent(cloneThen);

            cloneElse.setCondition(ctConditional.getElseExpression());
            ctConditional.getElseExpression().setParent(cloneElse);

            ctConditional.getThenExpression().setTypeCasts(ctConditional.getThenExpression().getTypeCasts());
            ctConditional.getElseExpression().setTypeCasts(ctConditional.getElseExpression().getTypeCasts());

            anIf.setThenStatement(cloneThen);
            anIf.setElseStatement(cloneElse);
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
        CtAssignment assignmentThen = getFactory().Core().clone(assignment);
        assignmentThen.setAssignment(ctConditional.getThenExpression());
        assignmentThen.setParent(anIf);
        anIf.setThenStatement(assignmentThen);

        CtAssignment assignmentElse = getFactory().Core().clone(assignment);
        assignmentElse.setAssignment(ctConditional.getElseExpression());
        assignmentElse.setParent(anIf);
        anIf.setThenStatement(assignmentElse);

        assignmentThen.setTypeCasts(ctConditional.getThenExpression().getTypeCasts());
        assignmentElse.setTypeCasts(ctConditional.getElseExpression().getTypeCasts());

        anIf.setElseStatement(assignmentElse);
        anIf.setThenStatement(assignmentThen);
    }
}
