package fr.inria.spirals.npefix.transformer.processors;

import spoon.reflect.code.*;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.util.List;


public class ProcessorUtility {

    public static boolean isStaticAndFinal(CtTargetedExpression element) {
        if (element instanceof CtFieldAccess<?> &&
                ((CtFieldAccess) element).getVariable().isStatic() &&
                ((CtFieldAccess) element).getVariable().isFinal())
            return true;
        if (element instanceof CtInvocation<?> &&
                ((CtInvocation) element).getExecutable().isStatic())
            return true;
        return false;
    }

    public static CtExpression removeUnaryOperator(CtExpression assigned, boolean beforeStatement) {
        assigned = assigned.getFactory().Core().clone(assigned);
        List<CtUnaryOperator> elements = assigned.getElements(new AbstractFilter<CtUnaryOperator>(CtUnaryOperator.class) {
            @Override
            public boolean matches(CtUnaryOperator element) {
                UnaryOperatorKind kind = element.getKind();
                return kind.equals(UnaryOperatorKind.PREDEC)
                        || kind.equals(UnaryOperatorKind.PREINC)
                        || kind.equals(UnaryOperatorKind.POSTDEC)
                        || kind.equals(UnaryOperatorKind.POSTINC);
            }
        });
        if (elements.size() == 0) {
            return assigned;
        }

        for (int k = 0; k < elements.size(); k++) {
            CtUnaryOperator ctUnaryOperator = elements.get(k);
            CtExpression operand = ctUnaryOperator.getOperand();
            UnaryOperatorKind kind = ctUnaryOperator.getKind();

            BinaryOperatorKind operator = null;
            if (!beforeStatement) {
                if (kind.equals(UnaryOperatorKind.POSTDEC)) {
                    operator = BinaryOperatorKind.PLUS;
                } else if (kind.equals(UnaryOperatorKind.POSTINC)) {
                    operator = BinaryOperatorKind.MINUS;
                }
            } else {
                if (kind.equals(UnaryOperatorKind.PREDEC)) {
                    operator = BinaryOperatorKind.MINUS;
                } else if (kind.equals(UnaryOperatorKind.PREINC)) {
                    operator = BinaryOperatorKind.PLUS;
                }
            }
            if (operator != null) {
                operand = assigned.getFactory().Code().createBinaryOperator(operand,
                        assigned.getFactory().Code().createLiteral(1),
                        operator);
            }
            operand.setParent(ctUnaryOperator.getParent());
            ctUnaryOperator.replace(operand);
        }

        return assigned;
    }

    public static CtExpression createCtTypeElement(CtTypeReference targetType) {
        if(targetType == null) {
            return null;
        }
        CtExpression ctType;
        if (targetType == null ||
                targetType.isAnonymous() ||
                targetType.getSimpleName() == null ||
                (targetType.getPackage() == null && targetType.getSimpleName().length() == 1)) {
            ctType = targetType.getFactory().Core().createLiteral();
            ctType.setType(targetType.getFactory().Type().nullType());
        } else {
            int countArray = 0;
            while (targetType instanceof CtArrayTypeReference) {
                countArray++;
                if (((CtArrayTypeReference) targetType).getComponentType() instanceof CtTypeParameterReference) {
                    ctType = targetType.getFactory().Core().createLiteral();
                    ctType.setType(targetType.getFactory().Type().nullType());
                    return ctType;
                } else {
                    targetType = ((CtArrayTypeReference) targetType).getComponentType();
                }
            }
            String targetTypeName = targetType.getQualifiedName();
            for (int i = 0; i < countArray; i++) {
                targetTypeName += "[]";
            }
            targetType = targetType.getFactory().Type().createReference(targetTypeName);

            CtFieldReference<Object> ctfe = targetType.getFactory().Core().createFieldReference();
            ctfe.setSimpleName("class");
            ctfe.setDeclaringType(targetType);
            ctfe.setType(targetType.getFactory().Code().createCtTypeReference(Class.class));

            ctType = targetType.getFactory().Core().createFieldRead();
            ((CtFieldAccess) ctType).setVariable(ctfe);
            ctType.setType(targetType.getFactory().Code().createCtTypeReference(Class.class));
        }
        ctType.getTypeCasts().clear();
        return ctType;
    }
}
