
// PACKAGE/IMPORTS --------------------------------------------------
package com.gk.dal.specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.gk.dal.search.SpecificationSearchCriteria;

/**
 * @author Gaurav_Singh3
 *
 */
public class EntitySpecification<C> implements Specification<C> {

    /**
     * 
     */
    private static final long serialVersionUID = 7356023004931974231L;

    private final EntitySpecificationHelper cloudEntitySpecificationHelper;

    /**
     * 
     */

    private final SpecificationSearchCriteria criteria;

    /**
     * @param criteria
     */
    public EntitySpecification(final SpecificationSearchCriteria criteria) {
        super();
        this.criteria = criteria;
        this.cloudEntitySpecificationHelper = new EntitySpecificationHelper();
    }

    @Override
    public Predicate toPredicate(final Root<C> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        return cloudEntitySpecificationHelper.createPredicate(root, builder, criteria);
    }

}
