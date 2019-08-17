
// PACKAGE/IMPORTS --------------------------------------------------
package com.gk.dal.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * @author Satyajit_Dessai
 *
 */
@Component
public class SpecificationSearchCriteriaCreatorImpl implements SpecificationSearchCriteriaCreator {
    private static Pattern commaSeparatedNumericRegex = Pattern.compile("^[\\d|\\.]+(?:[ \\t]*,[ \\t]*[\\d|\\.]+)+$");

    @Override
    public SpecificationSearchCriteria create(final String key, final String operation, final String prefix, final String value, final String suffix) {
        // TagSearchField tagSearchField = getField(key);
        Class<?> type = String.class;
        if (key.equalsIgnoreCase("deleted") || key.equalsIgnoreCase("updated")) {
            type = Boolean.class;
        }
        SearchOperators op = SearchOperators.getSimpleOperation(operation);
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
        final Object objValue = prepareSpecificatoinSearchCriteria(key, type, op, value);
        final SpecificationSearchCriteria criteria = new SpecificationSearchCriteria(key, op, objValue);

        return criteria;
    }

    private Object prepareSpecificatoinSearchCriteria(final String key, final Class<?> type, final SearchOperators op, final String value) {

        if (type.equals(String.class)) {
            if (op == SearchOperators.IN || op == SearchOperators.NOTIN) {
                return Arrays.asList(value.split("\\s*,\\s*"));
            }
            return value;
        } else if (type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value) ? 1 : 0;
        } else {
            if (op == SearchOperators.IN || op == SearchOperators.NOTIN) {
                final Matcher matcher = commaSeparatedNumericRegex.matcher(value.trim());
                if (matcher.find()) {
                    final List<String> stringlist = Arrays.asList(value.split("\\s*,\\s*"));
                    if (type.equals(Integer.class)) {
                        final List<Integer> intList = new ArrayList<>();
                        for (final String strVal : stringlist) {
                            intList.add(Integer.parseInt(strVal));
                        }
                        return intList;
                    } else if (type.equals(Long.class)) {
                        final List<Long> longList = new ArrayList<>();
                        for (final String strVal : stringlist) {
                            longList.add(Long.parseLong(strVal));
                        }
                        return longList;
                    } else if (type.equals(Double.class)) {
                        final List<Double> doubleList = new ArrayList<>();
                        for (final String strVal : stringlist) {
                            doubleList.add(Double.parseDouble(strVal));
                        }
                        return doubleList;
                    }
                }
            }
            return parseObject(key, value);
        }

    }

    private Object parseObject(final String key, final String value) {
        return value;
    }

}
