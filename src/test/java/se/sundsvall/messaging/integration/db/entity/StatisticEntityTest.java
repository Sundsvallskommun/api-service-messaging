package se.sundsvall.messaging.integration.db.entity;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticEntityTest {

	@Test
	void testBuilder() {
		final var id = 1234L;
		final var messageType = MessageType.SLACK;
		final var originalMessageType = MessageType.EMAIL;
		final var status = MessageStatus.NO_CONTACT_WANTED;
		final var origin = "origin";
		final var department = "department";
		final var createdAt = LocalDateTime.now();
		final var municipalityId = "municipalityId";

		final var bean = StatisticEntity.builder()
			.withCreatedAt(createdAt)
			.withDepartment(department)
			.withId(id)
			.withMessageType(messageType)
			.withMunicipalityId(municipalityId)
			.withOrigin(origin)
			.withOriginalMessageType(originalMessageType)
			.withStatus(status)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getCreatedAt()).isEqualTo(createdAt);
		assertThat(bean.getDepartment()).isEqualTo(department);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getMessageType()).isEqualTo(messageType);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getOriginalMessageType()).isEqualTo(originalMessageType);
		assertThat(bean.getStatus()).isEqualTo(status);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(StatisticEntity.builder().build()).hasAllNullFieldsOrProperties();
	}
}
