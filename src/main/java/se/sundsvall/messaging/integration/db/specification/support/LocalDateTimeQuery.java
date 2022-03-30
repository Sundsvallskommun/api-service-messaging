package se.sundsvall.messaging.integration.db.specification.support;

import java.time.LocalDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

public class LocalDateTimeQuery<ENTITY, METAMODEL> extends Query<LocalDateTime, ENTITY, METAMODEL> {

    private final Mode mode;

    private LocalDateTimeQuery(final SingularAttribute<ENTITY, LocalDateTime> attribute, final LocalDateTime value, final Mode mode) {
        super(attribute, value);

        this.mode = mode;
    }

    public static <ENTITY, METAMODEL> LocalDateTimeQuery<ENTITY, METAMODEL> lt(
            final SingularAttribute<ENTITY, LocalDateTime> attribute, final LocalDateTime value) {
        return new LocalDateTimeQuery<>(attribute, value, Mode.LESS_THAN);
    }

    public static <ENTITY, METAMODEL> LocalDateTimeQuery<ENTITY, METAMODEL> lte(
            final SingularAttribute<ENTITY, LocalDateTime> attribute, final LocalDateTime value) {
        return new LocalDateTimeQuery<>(attribute, value, Mode.LESS_THAN_OR_EQUAL);
    }

    public static <ENTITY, METAMODEL> LocalDateTimeQuery<ENTITY, METAMODEL> equalTo(
            final SingularAttribute<ENTITY, LocalDateTime> attribute, final LocalDateTime value) {
        return new LocalDateTimeQuery<>(attribute, value, Mode.EQUAL_TO);
    }

    public static <ENTITY, METAMODEL> LocalDateTimeQuery<ENTITY, METAMODEL> gte(
            final SingularAttribute<ENTITY, LocalDateTime> attribute, final LocalDateTime value) {
        return new LocalDateTimeQuery<>(attribute, value, Mode.GREATER_THAN_OR_EQUAL);
    }

    public static <ENTITY, METAMODEL> LocalDateTimeQuery<ENTITY, METAMODEL> gt(
            final SingularAttribute<ENTITY, LocalDateTime> attribute, final LocalDateTime value) {
        return new LocalDateTimeQuery<>(attribute, value, Mode.GREATER_THAN);
    }

    @Override
    public Predicate eval(final Root<ENTITY> root, final CriteriaBuilder criteriaBuilder) {
        return switch (mode) {
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(attribute), value);
            case LESS_THAN_OR_EQUAL -> criteriaBuilder.lessThanOrEqualTo(root.get(attribute), value);
            case EQUAL_TO -> criteriaBuilder.equal(root.get(attribute), value);
            case GREATER_THAN_OR_EQUAL -> criteriaBuilder.greaterThanOrEqualTo(root.get(attribute), value);
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(attribute), value);
        };
    }
}
