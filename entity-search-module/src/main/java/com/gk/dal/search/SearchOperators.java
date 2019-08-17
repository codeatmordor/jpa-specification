package com.gk.dal.search;

/**
 * @author Gaurav_Singh3
 *
 */
public enum SearchOperators {
    EQUAL("="), NOTEQUAL("!="), GREATER_THAN(">"), LESS_THAN("<"), LIKE("like"), IN("in"), NOTIN("not_in"), STARTS_WITH("~startwith~"), ENDS_WITH("~endswith~"), CONTAINS("~contains~");

    public static final String[] SIMPLE_OPERATION_SET = { "=", "!=", "<>", "like", "in", "not_in" };

    public static final String ZERO_OR_MORE_REGEX = "*";

    public static final String OR_OPERATOR = "OR";

    public static final String AND_OPERATOR = "AND";

    public static final String LEFT_PARANTHESIS = "(";

    public static final String RIGHT_PARANTHESIS = ")";

    String value;

    SearchOperators(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SearchOperators fromString(final String value) {
        switch (value) {
            case "=":
                return EQUAL;
            case "like":
                return LIKE;
        }
        return null;
    }

    public static SearchOperators getSimpleOperation(final String input) {
        switch (input) {
            case "=":
                return EQUAL;
            case "!=":
                return NOTEQUAL;
            case ">":
                return GREATER_THAN;
            case "<":
                return LESS_THAN;
            case "~":
                return LIKE;
            case "in":
                return IN;
            case "not_in":
                return NOTIN;
            case "like":
                return LIKE;
            default:
                return null;
        }
    }
}
