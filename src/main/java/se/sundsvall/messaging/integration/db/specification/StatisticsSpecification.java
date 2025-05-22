package se.sundsvall.messaging.integration.db.specification;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.MessageType;

public interface StatisticsSpecification {

	SpecificationBuilder<HistoryEntity> BUILDER = new SpecificationBuilder<>();
	String ORIGINAL_MESSAGE_TYPE = "originalMessageType";
	String MUNICIPALITY_ID = "municipalityId";
	String ORIGIN = "origin";
	String DEPARTMENT = "department";
	String CREATED_AT = "createdAt";

	static Specification<HistoryEntity> withOriginalMessageTypeIn(final List<MessageType> messageTypes) {
		var messageTypesAsString = messageTypes.stream()
			.map(MessageType::name)
			.toList();
		return BUILDER.buildInFilter(ORIGINAL_MESSAGE_TYPE, messageTypesAsString);
	}

	static Specification<HistoryEntity> withMunicipalityId(final String municipalityId) {
		return BUILDER.buildEqualFilter(MUNICIPALITY_ID, municipalityId);
	}

	static Specification<HistoryEntity> withOrigin(final String origin) {
		return BUILDER.buildEqualFilter(ORIGIN, origin);
	}

	static Specification<HistoryEntity> withDepartment(final String department) {
		return BUILDER.buildEqualFilter(DEPARTMENT, department);
	}

	static Specification<HistoryEntity> withCreatedAtAfter(final LocalDateTime from) {
		return BUILDER.buildDateIsEqualOrAfterFilter(CREATED_AT, from);
	}

	static Specification<HistoryEntity> withCreatedAtBefore(final LocalDateTime to) {
		return BUILDER.buildDateIsEqualOrBeforeFilter(CREATED_AT, to);
	}

}
