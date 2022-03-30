package se.sundsvall.messaging.integration.db.specification.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public class StringQuery<ENTITY, METAMODEL> extends Query<String, ENTITY, METAMODEL> {

    private final boolean equalTo;

    private StringQuery(final SingularAttribute<ENTITY, String> attribute, final String value,
            final boolean equalTo) {
        super(attribute, value);

        this.equalTo = equalTo;
    }

    public static <ENTITY, METAMODEL> StringQuery<ENTITY, METAMODEL> equalTo(
            final SingularAttribute<ENTITY, String> attribute,
            final String value) {
        return new StringQuery<>(attribute, value, true);
    }

    public static <ENTITY, METAMODEL> StringQuery<ENTITY, METAMODEL> like(
            final SingularAttribute<ENTITY, String> attribute,
            final String value) {
        return new StringQuery<>(attribute, value, false);
    }

    @Override
    public Predicate eval(final Root<ENTITY> root, final CriteriaBuilder criteriaBuilder) {
        if (equalTo) {
            return criteriaBuilder.equal(root.get(attribute), value);
        } else {
            return criteriaBuilder.like(root.get(attribute), value);
        }
    }
}
