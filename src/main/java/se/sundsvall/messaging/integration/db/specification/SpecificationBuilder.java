package se.sundsvall.messaging.integration.db.specification;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {

	public Specification<T> buildEqualFilter(final String attribute, final String value) {
		return (entity, cq, cb) -> {
			if (value != null) {
				return cb.equal(entity.get(attribute), value);
			}
			return cb.and();
		};
	}

	public Specification<T> buildDateIsEqualOrBeforeFilter(final String attribute, final LocalDateTime value) {
		return (entity, cq, cb) -> {
			if (value != null) {
				return cb.lessThanOrEqualTo(entity.get(attribute), value);
			}
			return cb.and();
		};
	}

	public Specification<T> buildDateIsEqualOrAfterFilter(final String attribute, final LocalDateTime value) {
		return (entity, cq, cb) -> {
			if (value != null) {
				return cb.greaterThanOrEqualTo(entity.get(attribute), value);
			}
			return cb.and();
		};
	}

	public Specification<T> buildInFilter(final String attribute, final List<String> values) {
		return (root, query, cb) -> {
			if (values == null || values.isEmpty()) {
				return cb.and();
			}
			return root.get(attribute).in(values);
		};
	}

}
