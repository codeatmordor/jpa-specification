
// PACKAGE/IMPORTS --------------------------------------------------
package com.gk.dal.search;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.json.JSONObject;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import com.gk.dal.specification.EntitySpecification;
import com.gk.dal.specification.EntitySpecificationBuilder;
import com.symantec.cloudentity.client.CloudEntityServiceConstants;
import com.symantec.cloudentity.service.dal.search.CloudEntityServiceHelper;
import com.symantec.cloudentity.service.dal.search.CriteriaParser;
import com.symantec.cloudentity.service.dal.search.EntityOffsetBasedPageRequest;
import com.symantec.cloudentity.service.dal.search.SpecificationSearchCriteriaCreator;
import com.symantec.cloudentity.service.dao.entities.CustomerDomainEntity;
import com.symantec.cloudentity.service.dto.PaginationRequest;
import com.symantec.cloudentity.service.dto.SearchCloudEntityDto;
import com.symantec.cloudentity.service.exception.CloudEntityServiceException;
import com.symantec.datasource.router.route.ForceReadOnlyContextHolder;
import com.symantec.dcs.cloud.common.JsonHelper;
import com.symantec.epmp.common.context.RequestContext;
import com.symantec.epmp.common.context.RequestContextHolder;

/**
 * @author Gaurav_Singh3
 *
 */
public class CloudEntityServiceHelper {

    private static final XLogger log = XLoggerFactory.getXLogger(CloudEntityServiceHelper.class);

    private static Map<String, Integer> customerDomainMap = new HashMap<>();

    private static final String HEADER_FORCE_READONLY = "x-epmp-use-replica";

    public static String prepareDomainSpecificWhereClause(final String whereClause, final Integer domainId) {
        final StringBuilder domainSpecificWhereClauseBldr = new StringBuilder();
        if (whereClause != null && !whereClause.isEmpty()) {
            domainSpecificWhereClauseBldr.append(whereClause);
            domainSpecificWhereClauseBldr.append(" AND ");
        }
        domainSpecificWhereClauseBldr.append(String.format(" customerDomainId=%d ", domainId));

        return domainSpecificWhereClauseBldr.toString();
    }

    public static <T> Specification<T> resolveSpecification(final String searchParameters, final SpecificationSearchCriteriaCreator specSearchCriteriaCreator) throws CloudEntityServiceException {
        final CriteriaParser criteriaParser = new CriteriaParser();
        final EntitySpecificationBuilder<T> specificationBuilder = new EntitySpecificationBuilder<>();
        return specificationBuilder.build(criteriaParser.parse(searchParameters, specSearchCriteriaCreator), EntitySpecification<T>::new);
    }

    public static void validateEntityInputRequest(final Integer inputSize, final Integer limit) throws CloudEntityServiceException {
        if (inputSize > limit) {
            throw new CloudEntityServiceException("input.size.exceeds", HttpStatus.PAYLOAD_TOO_LARGE, new Object[] { Integer.toString(limit) }, null);
        }
    }

    public static void checkAndSetReadOnlyFlag(final RequestContext requestContext, final Boolean paramValue) {
        Boolean forceReadOnlyValue = paramValue;
        if (!forceReadOnlyValue) {
            final String value = requestContext.getHeader(HEADER_FORCE_READONLY);
            if (value != null) {
                forceReadOnlyValue = Boolean.valueOf(value);
            }
        }
        ForceReadOnlyContextHolder.setForceReadOnly(forceReadOnlyValue);
    }

    public static String prepareEntitySpecificWhereClause(final String whereClause, final String entityId, final EntityType entityType) throws CloudEntityServiceException {
        final StringBuilder entitySpecificWhereClauseBldr = new StringBuilder();
        if (whereClause != null && !whereClause.isEmpty()) {
            entitySpecificWhereClauseBldr.append(whereClause);
            entitySpecificWhereClauseBldr.append(" AND ");
        }
        if (entityType == EntityType.CLOUDACCOUNT)
            entitySpecificWhereClauseBldr.append(String.format(" account_id=%s", entityId));
        else if (entityType == EntityType.ADAPTER_CONFIG)
            entitySpecificWhereClauseBldr.append(String.format(" adapter_config_id=%s", entityId));
        else if (entityType == EntityType.VPC)
            entitySpecificWhereClauseBldr.append(String.format(" vpc_id=%s", entityId));
        else if (entityType == EntityType.COMPARTMENT)
            entitySpecificWhereClauseBldr.append(String.format(" compartment_id=%s", entityId));
        else {
            throw new CloudEntityServiceException("invalid.parent.entity", HttpStatus.BAD_REQUEST, new Object[] { entityType.toString() }, null);
        }

        return entitySpecificWhereClauseBldr.toString();
    }

    public static Pageable preparePageable(final SearchCloudEntityDto searchDto, PaginationRequest paginationRequest) {
        Sort sort = null;
        if (StringUtils.isNotBlank(searchDto.getSortBy())) {
            final List<String> sortByList = Arrays.asList(searchDto.getSortBy().split("\\s*,\\s*"));
            final Direction direction = searchDto.getSortOrder() != null && searchDto.getSortOrder().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = new Sort(direction, sortByList.stream().toArray(String[]::new));
            System.out.println(JsonHelper.convertToJson(sort));
        }
        if (paginationRequest == null) {
            paginationRequest = new PaginationRequest(CloudEntityServiceConstants.DEFAULT_LIMIT, 0);
        }
        final Pageable pageable = (sort != null) ? new EntityOffsetBasedPageRequest(paginationRequest.getOffset(), paginationRequest.getLimit(), sort) : new EntityOffsetBasedPageRequest(paginationRequest.getOffset(), paginationRequest.getLimit());
        return pageable;
    }

    @SuppressWarnings("unchecked")
    public static CriteriaQuery getDefaultCriteria(final Integer customerDomainId, @SuppressWarnings("rawtypes") final Class entityClass, final Session session, final Boolean deleted, final Integer offset, final Integer limit, final String orderBy, final String sortBy) {
        // Create CriteriaBuilder
        final CriteriaBuilder builder = session.getCriteriaBuilder();

        // Create CriteriaQuery
        final CriteriaQuery criteriaQuery = builder.createQuery(entityClass);
        //
        final Root root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);
        if (StringUtils.isEmpty(orderBy) && StringUtils.isEmpty(sortBy))
            criteriaQuery.orderBy(builder.asc(root.get("created")));

        if (StringUtils.isNotEmpty(sortBy)) {
            if (StringUtils.isEmpty(orderBy) || "ASC".equalsIgnoreCase(orderBy))
                criteriaQuery.orderBy(builder.asc(root.get(sortBy)));
            else if ("DESC".equalsIgnoreCase(orderBy))
                criteriaQuery.orderBy(builder.desc(root.get(sortBy)));
            else
                criteriaQuery.orderBy(builder.asc(root.get("created")));
        }

        if (Objects.nonNull(customerDomainId))
            criteriaQuery.where(builder.equal(root.get("customer_domain_id"), customerDomainId));

        if (Objects.nonNull(deleted))
            criteriaQuery.where(builder.equal(root.get("deleted"), deleted));

        /*
         * Query limitedCriteriaQuery = session.createQuery(criteriaQuery); if
         * (Objects.nonNull(limit)) limitedCriteriaQuery.setMaxResults(limit);
         * if (Objects.nonNull(offset))
         * limitedCriteriaQuery.setFirstResult(offset);
         */
        return criteriaQuery;
    }

    public static StringBuilder getAppHomeConfigDirectory() {
        return new StringBuilder().append("file:").append(System.getProperty("APP_HOME")).append("/files");
    }

    public static JSONObject decodeOAuth2Token() {
        log.debug("Decoding access token...");
        // log.info("Token for issue ="
        // +RequestContextHolder.getContext().getOAuth2Token() );
        final String sub = new JSONObject(new String(Base64.getDecoder().decode(RequestContextHolder.getContext().getOAuth2Token().split("\\.")[1]))).getString("sub");
        log.debug("sub {}", sub);
        return new JSONObject(sub);
    }

    public static <T extends Enum<?>> T searchEnum(final Class<T> enumeration, final String search) {
        for (final T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(search) == 0) {
                return each;
            }
        }
        return null;
    }

    public static int getDomainId() throws CloudEntityServiceException {
        final String customerId = RequestContextHolder.getContext().getCustomerId();
        final String domainId = RequestContextHolder.getContext().getDomainId();
        if (StringUtils.isEmpty(customerId) || StringUtils.isEmpty(domainId))
            throw new CloudEntityServiceException("Invalid_Arguments", HttpStatus.BAD_REQUEST, null, null);
        final String key = customerId.concat(":").concat(domainId);
        final Integer customerDomainId = customerDomainMap.get(key);
        return customerDomainId == null ? -1 : customerDomainId;
    }

    public static void addToDomainMap(final CustomerDomainEntity customerDomainEntity) {
        final String key = customerDomainEntity.getCustomerId().concat(":").concat(customerDomainEntity.getDomainId());
        customerDomainMap.put(key, customerDomainEntity.getId());
    }

    public static Timestamp convertToTimestamp(final String date) throws CloudEntityServiceException {
        if (date == null || date.isEmpty()) {
            return null;
        }
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new Timestamp(dateFormat.parse(date).getTime());
        } catch (final ParseException parseException) {
            throw new CloudEntityServiceException("invalid.date.format", HttpStatus.BAD_REQUEST, new String[] { date }, new String[] { "yyyy-MM-dd HH:mm:ss" });

        }
    }

}
