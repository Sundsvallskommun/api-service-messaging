package se.sundsvall.messaging.integration.db.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class MessageMapperTest {
	private static final String BATCH_ID = "someBatchId";
	private static final String MESSAGE_ID = "someMessageId";
	private static final String DELIVERY_ID = "someDeliveryId";
	private static final String PARTY_ID = "somePartyId";
	private static final MessageType TYPE = SNAIL_MAIL;
	private static final MessageType ORIGINAL_TYPE = DIGITAL_MAIL;
	private static final MessageStatus STATUS = FAILED;
	private static final String CONTENT = "someContent";
	private static final String ORIGIN = "someOrigin";
	private static final String ISSUER = "someIssuer";
	private static final String MUNICIPALITY_ID = "someMunicipalityId";

	@Test
	void mapToMessageWhenMessageEntityIsNull() {
		assertThat(MessageMapper.mapToMessage(null)).isNull();
	}

	@Test
	void mapToMessage() {
		final var messageEntity = MessageEntity.builder()
			.withBatchId(BATCH_ID)
			.withMessageId(MESSAGE_ID)
			.withDeliveryId(DELIVERY_ID)
			.withPartyId(PARTY_ID)
			.withType(TYPE)
			.withOriginalMessageType(ORIGINAL_TYPE)
			.withStatus(STATUS)
			.withContent(CONTENT)
			.withOrigin(ORIGIN)
			.withIssuer(ISSUER)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		final var message = MessageMapper.mapToMessage(messageEntity);

		assertThat(message).isNotNull().hasNoNullFieldsOrPropertiesExcept("address");
		assertThat(message.batchId()).isEqualTo(BATCH_ID);
		assertThat(message.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(message.deliveryId()).isEqualTo(DELIVERY_ID);
		assertThat(message.partyId()).isEqualTo(PARTY_ID);
		assertThat(message.type()).isEqualTo(TYPE);
		assertThat(message.originalType()).isEqualTo(ORIGINAL_TYPE);
		assertThat(message.status()).isEqualTo(STATUS);
		assertThat(message.content()).isEqualTo(CONTENT);
		assertThat(message.origin()).isEqualTo(ORIGIN);
		assertThat(message.issuer()).isEqualTo(ISSUER);
		assertThat(message.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	@Test
	void mapToMessageEntityWhenMessageIsNull() {
		assertThat(MessageMapper.mapToMessageEntity(null)).isNull();
	}

	@Test
	void mapToMessageEntity() {
		final var message = Message.builder()
			.withBatchId(BATCH_ID)
			.withMessageId(MESSAGE_ID)
			.withDeliveryId(DELIVERY_ID)
			.withPartyId(PARTY_ID)
			.withType(TYPE)
			.withOriginalType(ORIGINAL_TYPE)
			.withStatus(STATUS)
			.withContent(CONTENT)
			.withOrigin(ORIGIN)
			.withIssuer(ISSUER)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		final var messageEntity = MessageMapper.mapToMessageEntity(message);

		assertThat(messageEntity).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "createdAt");
		assertThat(messageEntity.getBatchId()).isEqualTo(BATCH_ID);
		assertThat(messageEntity.getMessageId()).isEqualTo(MESSAGE_ID);
		assertThat(messageEntity.getDeliveryId()).isEqualTo(DELIVERY_ID);
		assertThat(messageEntity.getPartyId()).isEqualTo(PARTY_ID);
		assertThat(messageEntity.getType()).isEqualTo(TYPE);
		assertThat(messageEntity.getOriginalMessageType()).isEqualTo(ORIGINAL_TYPE);
		assertThat(messageEntity.getStatus()).isEqualTo(STATUS);
		assertThat(messageEntity.getContent()).isEqualTo(CONTENT);
		assertThat(messageEntity.getOrigin()).isEqualTo(ORIGIN);
		assertThat(messageEntity.getIssuer()).isEqualTo(ISSUER);
		assertThat(messageEntity.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
	}
}
