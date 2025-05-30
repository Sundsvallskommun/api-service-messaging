package se.sundsvall.messaging.integration.db.specification;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.messaging.integration.db.entity.StatisticEntity;
import se.sundsvall.messaging.model.MessageType;

public interface StatisticsSpecification {

	SpecificationBuilder<StatisticEntity> BUILDER = new SpecificationBuilder<>();
	String ORIGINAL_MESSAGE_TYPE = "originalMessageType";
	String MUNICIPALITY_ID = "municipalityId";
	String ORIGIN = "origin";
	String DEPARTMENT = "department";
	String CREATED_AT = "createdAt";

	static Specification<StatisticEntity> withOriginalMessageTypeIn(final List<MessageType> messageTypes) {
		final var messageTypesAsString = messageTypes.stream()
			.map(MessageType::name)
			.toList();
		return BUILDER.buildInFilter(ORIGINAL_MESSAGE_TYPE, messageTypesAsString);
	}

	static Specification<StatisticEntity> withMunicipalityId(final String municipalityId) {
		return BUILDER.buildEqualFilter(MUNICIPALITY_ID, municipalityId);
	}

	static Specification<StatisticEntity> withOrigin(final String origin) {
		return BUILDER.buildEqualFilter(ORIGIN, origin);
	}

	static Specification<StatisticEntity> withDepartment(final String department) {
		return BUILDER.buildEqualFilter(DEPARTMENT, department);
	}

	static Specification<StatisticEntity> withCreatedAtAfter(final LocalDateTime from) {
		return BUILDER.buildDateIsEqualOrAfterFilter(CREATED_AT, from);
	}

	static Specification<StatisticEntity> withCreatedAtBefore(final LocalDateTime to) {
		return BUILDER.buildDateIsEqualOrBeforeFilter(CREATED_AT, to);
	}

}
