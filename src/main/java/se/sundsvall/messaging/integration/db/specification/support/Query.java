package se.sundsvall.messaging.integration.db.specification.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public abstract class Query<PARAM, ENTITY, METAMODEL> {

    protected SingularAttribute<ENTITY, PARAM> attribute;
    protected PARAM value;

    public Query(final SingularAttribute<ENTITY, PARAM> attribute, final PARAM value) {
        this.attribute = attribute;
        this.value = value;
    }

    public abstract Predicate eval(final Root<ENTITY> root, final CriteriaBuilder criteriaBuilder);

    public PARAM getValue() {
        return value;
    }
}
