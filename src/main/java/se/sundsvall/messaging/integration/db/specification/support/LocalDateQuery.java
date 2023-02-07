package se.sundsvall.messaging.integration.db.specification.support;

import java.time.LocalDate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public class LocalDateQuery<E> extends Query<LocalDate, E> {

    private final Mode mode;

    private LocalDateQuery(final SingularAttribute<E, LocalDate> attribute, final LocalDate value, final Mode mode) {
        super(attribute, value);

        this.mode = mode;
    }

    public static <E> LocalDateQuery<E> lt(
            final SingularAttribute<E, LocalDate> attribute, final LocalDate value) {
        return new LocalDateQuery<>(attribute, value, Mode.LESS_THAN);
    }

    public static <E> LocalDateQuery<E> lte(
            final SingularAttribute<E, LocalDate> attribute, final LocalDate value) {
        return new LocalDateQuery<>(attribute, value, Mode.LESS_THAN_OR_EQUAL);
    }

    public static <E> LocalDateQuery<E> equalTo(
            final SingularAttribute<E, LocalDate> attribute, final LocalDate value) {
        return new LocalDateQuery<>(attribute, value, Mode.EQUAL_TO);
    }

    public static <E> LocalDateQuery<E> gte(
            final SingularAttribute<E, LocalDate> attribute, final LocalDate value) {
        return new LocalDateQuery<>(attribute, value, Mode.GREATER_THAN_OR_EQUAL);
    }

    public static <E> LocalDateQuery<E> gt(
            final SingularAttribute<E, LocalDate> attribute, final LocalDate value) {
        return new LocalDateQuery<>(attribute, value, Mode.GREATER_THAN);
    }

    @Override
    public Predicate eval(final Root<E> root, final CriteriaBuilder criteriaBuilder) {
        return switch (mode) {
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(attribute), value);
            case LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(root.get(attribute), value);
            case EQUAL_TO -> criteriaBuilder.equal(root.get(attribute), value);
            case GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(root.get(attribute), value);
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(attribute), value);
        };
    }
}
