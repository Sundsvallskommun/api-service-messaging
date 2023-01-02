package se.sundsvall.messaging.integration.db.specification.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public abstract class Query<P, E> {

    protected SingularAttribute<E, P> attribute;
    protected P value;

    protected Query(final SingularAttribute<E, P> attribute, final P value) {
        this.attribute = attribute;
        this.value = value;
    }

    public abstract Predicate eval(final Root<E> root, final CriteriaBuilder criteriaBuilder);

    public P getValue() {
        return value;
    }
}
