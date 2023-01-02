package se.sundsvall.messaging.integration.db.specification.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public class StringQuery<E> extends Query<String, E> {

    private final boolean equalTo;

    private StringQuery(final SingularAttribute<E, String> attribute, final String value,
            final boolean equalTo) {
        super(attribute, value);

        this.equalTo = equalTo;
    }

    public static <E> StringQuery<E> equalTo(
            final SingularAttribute<E, String> attribute,
            final String value) {
        return new StringQuery<>(attribute, value, true);
    }

    public static <E> StringQuery<E> like(
            final SingularAttribute<E, String> attribute,
            final String value) {
        return new StringQuery<>(attribute, value, false);
    }

    @Override
    public Predicate eval(final Root<E> root, final CriteriaBuilder criteriaBuilder) {
        if (equalTo) {
            return criteriaBuilder.equal(root.get(attribute), value);
        } else {
            return criteriaBuilder.like(root.get(attribute), value);
        }
    }
}
