package se.sundsvall.messaging.integration.db.entity;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class HistoryEntityTest {

	@Test
	void testBuilder() {
		final var id = 1234L;
		final var batchId = "batchId";
		final var messageId = "messageId";
		final var deliveryId = "deliveryId";
		final var partyId = "partyId";
		final var messageType = MessageType.SLACK;
		final var originalMessageType = MessageType.EMAIL;
		final var status = MessageStatus.NO_CONTACT_WANTED;
		final var statusDetail = "statusDetail";
		final var content = "content";
		final var origin = "origin";
		final var issuer = "issuer";
		final var department = "department";
		final var createdAt = LocalDateTime.now();
		final var municipalityId = "municipalityId";

		final var bean = HistoryEntity.builder()
			.withBatchId(batchId)
			.withContent(content)
			.withCreatedAt(createdAt)
			.withDeliveryId(deliveryId)
			.withDepartment(department)
			.withId(id)
			.withIssuer(issuer)
			.withMessageId(messageId)
			.withMessageType(messageType)
			.withMunicipalityId(municipalityId)
			.withOrigin(origin)
			.withOriginalMessageType(originalMessageType)
			.withPartyId(partyId)
			.withStatus(status)
			.withStatusDetail(statusDetail)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.getBatchId()).isEqualTo(batchId);
		assertThat(bean.getContent()).isEqualTo(content);
		assertThat(bean.getCreatedAt()).isEqualTo(createdAt);
		assertThat(bean.getDeliveryId()).isEqualTo(deliveryId);
		assertThat(bean.getDepartment()).isEqualTo(department);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getIssuer()).isEqualTo(issuer);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getMessageType()).isEqualTo(messageType);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getOriginalMessageType()).isEqualTo(originalMessageType);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getStatusDetail()).isEqualTo(statusDetail);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(HistoryEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new HistoryEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPrePersist() {
		final var bean = HistoryEntity.builder().build();
		assertThat(bean.getCreatedAt()).isNull();

		bean.prePersist();
		assertThat(bean.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
	}
}