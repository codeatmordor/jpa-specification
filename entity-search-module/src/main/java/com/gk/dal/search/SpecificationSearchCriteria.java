
// PACKAGE/IMPORTS --------------------------------------------------
package com.gk.dal.search;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gaurav_Singh3
 *
 */
public class SpecificationSearchCriteria {

    private String key;
    private SearchOperators operation;
    private Object value;

    public SpecificationSearchCriteria() {

    }

    public SpecificationSearchCriteria(final String key, final SearchOperators operation, final Object value) {
        super();
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecificationSearchCriteria(final String orPredicate, final String key, final SearchOperators operation, final Object value) {
        super();
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public SpecificationSearchCriteria(final String key, final String operation, final String prefix, final String value, final String suffix) {
        SearchOperators op = SearchOperators.getSimpleOperation(operation);
        this.key = key;
        this.value = value;
        if (op != null) {
            if (op == SearchOperators.EQUAL) {
                final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperators.ZERO_OR_MORE_REGEX);
                final boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperators.ZERO_OR_MORE_REGEX);

                if (startWithAsterisk && endWithAsterisk) {
                    op = SearchOperators.CONTAINS;
                } else if (startWithAsterisk) {
                    op = SearchOperators.ENDS_WITH;
                } else if (endWithAsterisk) {
                    op = SearchOperators.STARTS_WITH;
                }
            }
            if (op == SearchOperators.IN || op == SearchOperators.NOTIN) {
                final List<String> stringlist = Arrays.asList(value.split("\\s*,\\s*"));
                this.value = stringlist;
            }
        }
        this.operation = op;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public SearchOperators getOperation() {
        return operation;
    }

    public void setOperation(final SearchOperators operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

}
