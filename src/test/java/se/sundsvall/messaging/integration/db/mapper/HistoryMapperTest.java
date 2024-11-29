package se.sundsvall.messaging.integration.db.mapper;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.Message;

class HistoryMapperTest {

	@Test
	void mapToHistoryWhenHistoryEntityIsNull() {
		assertThat(HistoryMapper.mapToHistory(null)).isNull();
	}

	@Test
	void mapToHistoryFromHistoryEntity() {
		var historyEntity = HistoryEntity.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withMessageType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withMunicipalityId("someMunicipalityId")
			.withContent("someContent")
			.withOrigin("someOrigin")
			.withIssuer("someIssuer")
			.withOriginalMessageType(DIGITAL_MAIL)
			.withCreatedAt(LocalDateTime.now())
			.build();

		var history = HistoryMapper.mapToHistory(historyEntity);

		assertThat(history).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(history.batchId()).isEqualTo(historyEntity.getBatchId());
		assertThat(history.messageId()).isEqualTo(historyEntity.getMessageId());
		assertThat(history.deliveryId()).isEqualTo(historyEntity.getDeliveryId());
		assertThat(history.messageType()).isEqualTo(historyEntity.getMessageType());
		assertThat(history.status()).isEqualTo(historyEntity.getStatus());
		assertThat(history.content()).isEqualTo(historyEntity.getContent());
		assertThat(history.createdAt()).isEqualTo(historyEntity.getCreatedAt());
		assertThat(history.municipalityId()).isEqualTo(historyEntity.getMunicipalityId());
	}

	@Test
	void mapToHistoryEntityWhenMessageIsNull() {
		assertThat(HistoryMapper.mapToHistoryEntity(null, null)).isNull();
	}

	@Test
	void mapToHistoryEntity() {
		var statusDetail = "someStatusDetail";
		var address = Address.builder().withAddress("someAddress").build();
		var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withOriginalType(DIGITAL_MAIL)
			.withStatus(FAILED)
			.withContent("{\"department\":\"department\"}")
			.withOrigin("someOrigin")
			.withIssuer("someIssuer")
			.withMunicipalityId("someMunicipalityId")
			.withAddress(address)
			.build();

		var historyEntity = HistoryMapper.mapToHistoryEntity(message, statusDetail);

		assertThat(historyEntity).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "destinationAddressJson");
		assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
		assertThat(historyEntity.getOriginalMessageType()).isEqualTo(message.originalType());
		assertThat(historyEntity.getStatus()).isEqualTo(message.status());
		assertThat(historyEntity.getStatusDetail()).isEqualTo(statusDetail);
		assertThat(historyEntity.getContent()).isEqualTo(message.content());
		assertThat(historyEntity.getOrigin()).isEqualTo(message.origin());
		assertThat(historyEntity.getIssuer()).isEqualTo(message.issuer());
		assertThat(historyEntity.getDepartment()).isEqualTo("department");
		assertThat(historyEntity.getCreatedAt()).isNotNull().isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS));
		assertThat(historyEntity.getMunicipalityId()).isEqualTo(message.municipalityId());
		assertThat(historyEntity.getDestinationAddress()).isEqualTo(address);
	}

	@Test
	void mapToHistoryEntityNoDepartment() {
		var statusDetail = "someStatusDetail";
		var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withContent("{\"no-department\":\"someValue\"}")
			.withOrigin("someOrigin")
			.withMunicipalityId("someMunicipalityId")
			.build();

		var historyEntity = HistoryMapper.mapToHistoryEntity(message, statusDetail);

		assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
		assertThat(historyEntity.getStatus()).isEqualTo(message.status());
		assertThat(historyEntity.getStatusDetail()).isEqualTo(statusDetail);
		assertThat(historyEntity.getContent()).isEqualTo(message.content());
		assertThat(historyEntity.getOrigin()).isEqualTo(message.origin());
		assertThat(historyEntity.getCreatedAt()).isNotNull();
		assertThat(historyEntity.getDepartment()).isNull();
		assertThat(historyEntity.getMunicipalityId()).isEqualTo(message.municipalityId());
	}

	@Test
	void mapToHistoryEntityWhenContentIsNull() {
		var statusDetail = "someStatusDetail";
		var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withOrigin("someOrigin")
			.withMunicipalityId("someMunicipalityId")
			.build();

		var historyEntity = HistoryMapper.mapToHistoryEntity(message, statusDetail);

		assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
		assertThat(historyEntity.getStatus()).isEqualTo(message.status());
		assertThat(historyEntity.getStatusDetail()).isEqualTo(statusDetail);
		assertThat(historyEntity.getContent()).isEqualTo(message.content());
		assertThat(historyEntity.getOrigin()).isEqualTo(message.origin());
		assertThat(historyEntity.getCreatedAt()).isNotNull();
		assertThat(historyEntity.getDepartment()).isNull();
		assertThat(historyEntity.getMunicipalityId()).isEqualTo(message.municipalityId());
	}

}
