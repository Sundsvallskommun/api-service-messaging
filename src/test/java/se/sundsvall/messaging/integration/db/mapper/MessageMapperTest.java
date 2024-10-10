package se.sundsvall.messaging.integration.db.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Message;

class MessageMapperTest {

	@Test
	void mapToMessageWhenMessageEntityIsNull() {
		assertThat(MessageMapper.mapToMessage(null)).isNull();
	}

	@Test
	void mapToMessage() {
		final var messageEntity = MessageEntity.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withOriginalMessageType(DIGITAL_MAIL)
			.withStatus(FAILED)
			.withContent("someContent")
			.withOrigin("someOrigin")
			.withIssuer("someIssuer")
			.withMunicipalityId("someMunicipalityId")
			.build();

		final var message = MessageMapper.mapToMessage(messageEntity);

		assertThat(message).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(message.batchId()).isEqualTo(messageEntity.getBatchId());
		assertThat(message.messageId()).isEqualTo(messageEntity.getMessageId());
		assertThat(message.deliveryId()).isEqualTo(messageEntity.getDeliveryId());
		assertThat(message.partyId()).isEqualTo(messageEntity.getPartyId());
		assertThat(message.type()).isEqualTo(messageEntity.getType());
		assertThat(message.originalType()).isEqualTo(messageEntity.getOriginalMessageType());
		assertThat(message.status()).isEqualTo(messageEntity.getStatus());
		assertThat(message.content()).isEqualTo(messageEntity.getContent());
		assertThat(message.origin()).isEqualTo(messageEntity.getOrigin());
		assertThat(message.issuer()).isEqualTo(messageEntity.getIssuer());
		assertThat(message.municipalityId()).isEqualTo(messageEntity.getMunicipalityId());
	}

	@Test
	void mapToMessageEntityWhenMessageIsNull() {
		assertThat(MessageMapper.mapToMessageEntity(null)).isNull();
	}

	@Test
	void mapToMessageEntity() {
		final var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withOriginalType(DIGITAL_MAIL)
			.withStatus(FAILED)
			.withContent("someContent")
			.withOrigin("someOrigin")
			.withIssuer("someIssuer")
			.withMunicipalityId("someMunicipalityId")
			.build();

		final var messageEntity = MessageMapper.mapToMessageEntity(message);

		assertThat(messageEntity).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "createdAt");
		assertThat(messageEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(messageEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(messageEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(messageEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(messageEntity.getType()).isEqualTo(message.type());
		assertThat(messageEntity.getOriginalMessageType()).isEqualTo(message.originalType());
		assertThat(messageEntity.getStatus()).isEqualTo(message.status());
		assertThat(messageEntity.getContent()).isEqualTo(message.content());
		assertThat(messageEntity.getOrigin()).isEqualTo(message.origin());
		assertThat(messageEntity.getIssuer()).isEqualTo(message.issuer());
		assertThat(messageEntity.getMunicipalityId()).isEqualTo(message.municipalityId());
	}
}
