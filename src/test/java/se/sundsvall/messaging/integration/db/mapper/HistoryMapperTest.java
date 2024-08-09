package se.sundsvall.messaging.integration.db.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.Message;

class HistoryMapperTest {

	@Test
	void mapToHistoryWhenHistoryEntityIsNull() {
		assertThat(HistoryMapper.mapToHistory((HistoryEntity) null)).isNull();
	}

	@Test
	void mapToHistoryWhenStatsEntryIsNull() {
		assertThat(HistoryMapper.mapToHistory((StatsEntry) null)).isNull();
	}

	@Test
	void mapToHistoryFromHistoryEntity() {
		final var historyEntity = HistoryEntity.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withMessageType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withMunicipalityId("someMunicipalityId")
			.withContent("someContent")
			.withCreatedAt(LocalDateTime.now())
			.build();

		final var history = HistoryMapper.mapToHistory(historyEntity);

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
	void mapToHistoryFromStatsEntry() {
		final var statsEntry = new StatsEntry(SNAIL_MAIL, LETTER, FAILED, "2281");

		final var history = HistoryMapper.mapToHistory(statsEntry);

		assertThat(history.messageType()).isEqualTo(statsEntry.messageType());
		assertThat(history.originalMessageType()).isEqualTo(statsEntry.originalMessageType());
		assertThat(history.status()).isEqualTo(statsEntry.status());
		assertThat(history.municipalityId()).isEqualTo(statsEntry.municipalityId());
	}

	@Test
	void mapToHistoryEntityWhenMessageIsNull() {
		assertThat(HistoryMapper.mapToHistoryEntity(null, null)).isNull();
	}

	@Test
	void mapToHistoryEntity() {
		final var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withContent("{\"department\":\"department\"}")
			.withOrigin("someOrigin")
			.withMunicipalityId("someMunicipalityId")
			.build();

		var historyEntity = HistoryMapper.mapToHistoryEntity(message, "someStatusDetail");

		assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
		assertThat(historyEntity.getStatus()).isEqualTo(message.status());
		assertThat(historyEntity.getStatusDetail()).isEqualTo("someStatusDetail");
		assertThat(historyEntity.getContent()).isEqualTo(message.content());
		assertThat(historyEntity.getOrigin()).isEqualTo("someOrigin");
		assertThat(historyEntity.getDepartment()).isEqualTo("department");
		assertThat(historyEntity.getCreatedAt()).isNotNull();
		assertThat(historyEntity.getMunicipalityId()).isEqualTo("someMunicipalityId");
	}

	@Test
	void mapToHistoryEntityNoDepartment() {
		final var message = Message.builder()
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

		var historyEntity = HistoryMapper.mapToHistoryEntity(message, "someStatusDetail");

		assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
		assertThat(historyEntity.getStatus()).isEqualTo(message.status());
		assertThat(historyEntity.getStatusDetail()).isEqualTo("someStatusDetail");
		assertThat(historyEntity.getContent()).isEqualTo(message.content());
		assertThat(historyEntity.getOrigin()).isEqualTo("someOrigin");
		assertThat(historyEntity.getCreatedAt()).isNotNull();
		assertThat(historyEntity.getDepartment()).isNull();
		assertThat(historyEntity.getMunicipalityId()).isEqualTo("someMunicipalityId");
	}

	@Test
	void mapToHistoryEntityWhenContentIsNull() {
		final var message = Message.builder()
			.withBatchId("someBatchId")
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withPartyId("somePartyId")
			.withType(SNAIL_MAIL)
			.withStatus(FAILED)
			.withOrigin("someOrigin")
			.withMunicipalityId("someMunicipalityId")
			.build();

		var historyEntity = HistoryMapper.mapToHistoryEntity(message, "someStatusDetail");

		assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
		assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
		assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
		assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
		assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
		assertThat(historyEntity.getStatus()).isEqualTo(message.status());
		assertThat(historyEntity.getStatusDetail()).isEqualTo("someStatusDetail");
		assertThat(historyEntity.getContent()).isEqualTo(message.content());
		assertThat(historyEntity.getOrigin()).isEqualTo("someOrigin");
		assertThat(historyEntity.getCreatedAt()).isNotNull();
		assertThat(historyEntity.getDepartment()).isNull();
		assertThat(historyEntity.getMunicipalityId()).isEqualTo("someMunicipalityId");
	}

}
