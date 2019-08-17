
// PACKAGE/IMPORTS --------------------------------------------------
package com.gk.dal.specification;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;

import com.gk.dal.search.SearchOperators;
import com.gk.dal.search.SpecificationSearchCriteria;

/**
 * @author Gaurav_Singh3
 *
 */
public class EntitySpecificationBuilder<E> {

    public EntitySpecificationBuilder() {

    }

    public Specification<E> build(final Deque<?> postFixedExprStack, final Function<SpecificationSearchCriteria, Specification<E>> converter) {
        final Deque<Specification<E>> specStack = new LinkedList<>();
        Collections.reverse((List<?>) postFixedExprStack);
        while (!postFixedExprStack.isEmpty()) {
            final Object mayBeOperand = postFixedExprStack.pop();

            if (!(mayBeOperand instanceof String)) {
                specStack.push(converter.apply((SpecificationSearchCriteria) mayBeOperand));
            } else {
                final Specification<E> operand1 = specStack.pop();
                final Specification<E> operand2 = specStack.pop();
                if (mayBeOperand.equals(SearchOperators.AND_OPERATOR))
                    specStack.push(Specification.where(operand1).and(operand2));
                else if (mayBeOperand.equals(SearchOperators.OR_OPERATOR))
                    specStack.push(Specification.where(operand1).or(operand2));
            }

        }
        return specStack.pop();
    }
}
