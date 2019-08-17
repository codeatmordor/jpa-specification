
// PACKAGE/IMPORTS --------------------------------------------------
package com.gk.dal.specification;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.gk.dal.search.SpecificationSearchCriteria;
import com.google.common.collect.Lists;

/**
 * @author Gaurav_Singh3
 *
 */
public class EntitySpecificationHelper {

    public Predicate createPredicate(final Root<?> root, final CriteriaBuilder builder, final SpecificationSearchCriteria criteria) {
        Expression<String> path = null;
        if (criteria.getKey().equals("customerDomainId")) {
            path = root.join("customerDomain").get("id");
        } else if (criteria.getKey().equals("adapter_config_id")) {
            path = root.join("adapterConfig").get("id");
        } else if (criteria.getKey().equals("vpc_id")) {
            path = root.join("vpc").get("id");
        } else
            path = root.get(criteria.getKey());
        switch (criteria.getOperation()) {
            case EQUAL:
                return builder.equal(path, criteria.getValue());
            case NOTEQUAL:
                return builder.notEqual(path, criteria.getValue());
            case GREATER_THAN:
                return builder.greaterThan(path, criteria.getValue().toString());
            case LESS_THAN:
                return builder.lessThan(path, criteria.getValue().toString());
            case IN:
                return root.get(criteria.getKey()).in((List<String>) criteria.getValue());
            case NOTIN:
                return builder.not(root.get(criteria.getKey()).in((List<String>) criteria.getValue()));
            case LIKE:
                return builder.like(path, criteria.getValue().toString());
            case STARTS_WITH:
                return builder.like(path, criteria.getValue() + "%");
            case ENDS_WITH:
                return builder.like(path, "%" + criteria.getValue());
            case CONTAINS:
                return builder.like(path, "%" + criteria.getValue() + "%");
            default:
                return null;
        }

    }

    private Predicate setPathListValue(final SpecificationSearchCriteria criteria, final Expression<String> path) {
        if ("entity_type".equalsIgnoreCase(criteria.getKey())) {
            final List<Integer> nums = this.<Integer> getList(criteria.getKey(), criteria, Integer.class);
            return path.in(nums);
        }
        return path.in((List<String>) criteria.getValue());
    }

    private static HashMap<Class<?>, Function<String, ?>> parser = new HashMap<>();
    static {
        parser.put(boolean.class, Boolean::parseBoolean); // Support boolean
                                                          // literals too
        parser.put(int.class, Integer::parseInt);
        parser.put(long.class, Long::parseLong);
        parser.put(Boolean.class, Boolean::valueOf);
        parser.put(Integer.class, Integer::valueOf);
        parser.put(Long.class, Long::valueOf);
        parser.put(Double.class, Double::valueOf);
        parser.put(Float.class, Float::valueOf);
        parser.put(String.class, String::valueOf); // Handle String without
                                                   // special test
        // parser.put(BigDecimal.class, BigDecimal::new);
        // parser.put(BigInteger.class, BigInteger::new);
        // parser.put(LocalDate.class , LocalDate::parse); // Java 8 time API
    }

    private <T> List<T> GetList(final SpecificationSearchCriteria criteria, final Class<?> klass) {
        final List<String> stringlist = (List<String>) criteria.getValue();
        final T[] intArr = (T[]) Array.newInstance(klass, stringlist.size());
        int idx = 0;
        for (final String s : stringlist) {
            intArr[idx++] = (T) parse(s, klass);
        }
        final List<T> nums = Lists.newArrayList(intArr);
        return nums;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object parse(final String argString, final Class param) {
        final Function<String, ?> func = parser.get(param);
        if (func != null)
            return func.apply(argString);
        if (param.isEnum()) // Special handling for enums
            return Enum.valueOf(param, argString);
        throw new UnsupportedOperationException("Cannot parse string to " + param.getName());
    }

    private <T> List<T> getList(final String key, final SpecificationSearchCriteria criteria, final Class<T> klass) {
        return this.<T> GetList(criteria, klass);
    }
}
