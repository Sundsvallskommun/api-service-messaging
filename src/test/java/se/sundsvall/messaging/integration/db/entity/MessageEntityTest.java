package se.sundsvall.messaging.integration.db.entity;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageEntityTest {

	@Test
	void testBuilder() {
		var id = 1234L;
		var messageId = "messageId";
		var batchId = "batchId";
		var deliveryId = "deliveryId";
		var partyId = "partyId";
		var type = MessageType.SLACK;
		var originalMessageType = MessageType.EMAIL;
		var status = MessageStatus.NO_CONTACT_WANTED;
		var content = "content";
		var origin = "origin";
		var issuer = "issuer";
		var createdAt = LocalDateTime.now();
		var municipalityId = "municipalityId";
		var destinationAddress = Address.builder().withAddress("someAddress").build();

		var bean = MessageEntity.builder()
			.withBatchId(batchId)
			.withContent(content)
			.withCreatedAt(createdAt)
			.withDeliveryId(deliveryId)
			.withId(id)
			.withIssuer(issuer)
			.withMessageId(messageId)
			.withType(type)
			.withMunicipalityId(municipalityId)
			.withOrigin(origin)
			.withOriginalMessageType(originalMessageType)
			.withPartyId(partyId)
			.withStatus(status)
			.withDestinationAddress(destinationAddress)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("destinationAddressJson");
		assertThat(bean.getBatchId()).isEqualTo(batchId);
		assertThat(bean.getContent()).isEqualTo(content);
		assertThat(bean.getCreatedAt()).isEqualTo(createdAt);
		assertThat(bean.getDeliveryId()).isEqualTo(deliveryId);
		assertThat(bean.getId()).isEqualTo(id);
		assertThat(bean.getIssuer()).isEqualTo(issuer);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getType()).isEqualTo(type);
		assertThat(bean.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(bean.getOrigin()).isEqualTo(origin);
		assertThat(bean.getOriginalMessageType()).isEqualTo(originalMessageType);
		assertThat(bean.getPartyId()).isEqualTo(partyId);
		assertThat(bean.getStatus()).isEqualTo(status);
		assertThat(bean.getDestinationAddress()).isEqualTo(destinationAddress);
	}

	@Test
	void noDirtOnCreatedBean() {
		assertThat(MessageEntity.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new MessageEntity()).hasAllNullFieldsOrProperties();
	}

	@Test
	void testPrePersist() {
		var bean = MessageEntity.builder().build();
		assertThat(bean.getCreatedAt()).isNull();

		bean.prePersist();
		assertThat(bean.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
	}

}
