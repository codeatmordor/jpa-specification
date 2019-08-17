package com.gk.dal.search;

public interface SpecificationSearchCriteriaCreator {

    SpecificationSearchCriteria create(final String key, final String operation, final String prefix, final String value, final String suffix);
}
