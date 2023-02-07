package se.sundsvall.messaging.integration.db.specification.support;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class Spec<E> implements Specification<E> {

    private final transient Query<?, E> query;

    public Spec(final Query<?, E> query) {
        this.query = query;
    }

    @Override
    public Predicate toPredicate(@Nonnull final Root<E> root,
            @Nonnull final CriteriaQuery<?> criteriaQuery,
            @Nonnull final CriteriaBuilder criteriaBuilder) {
        if (query.getValue() == null) {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(true));   // Always true
        }

        return query.eval(root, criteriaBuilder);
    }
}
