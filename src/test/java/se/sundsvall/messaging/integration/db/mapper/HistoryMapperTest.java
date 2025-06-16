package se.sundsvall.messaging.integration.db.mapper;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.response.Batch;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.util.PagingUtil;

class HistoryMapperTest {

	@Test
	void mapToHistoryWhenHistoryEntityIsNull() {
		assertThat(HistoryMapper.mapToHistory(null)).isNull();
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
			.withOrigin("someOrigin")
			.withIssuer("someIssuer")
			.withOriginalMessageType(DIGITAL_MAIL)
			.withCreatedAt(LocalDateTime.now())
			.build();

		final var history = HistoryMapper.mapToHistory(historyEntity);

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
		final var statusDetail = "someStatusDetail";
		final var address = Address.builder().withAddress("someAddress").build();
		final var message = Message.builder()
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

		final var historyEntity = HistoryMapper.mapToHistoryEntity(message, statusDetail);

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
		final var statusDetail = "someStatusDetail";
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

		final var historyEntity = HistoryMapper.mapToHistoryEntity(message, statusDetail);

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
		final var statusDetail = "someStatusDetail";
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

		final var historyEntity = HistoryMapper.mapToHistoryEntity(message, statusDetail);

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
	void toBatch() {
		final var batchId = "batchId";
		final var sent = LocalDateTime.now();
		final var messageType = "messageType";
		final var subject = "subject";
		final var attachmentCount = 123;
		final var recipientCount = 456;
		final var status = Batch.Status.builder().build();

		final var bean = HistoryMapper.toBatch(batchId, sent, messageType, subject, attachmentCount, recipientCount, status);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.attachmentCount()).isEqualTo(attachmentCount);
		assertThat(bean.batchId()).isEqualTo(batchId);
		assertThat(bean.messageType()).isEqualTo(messageType);
		assertThat(bean.recipientCount()).isEqualTo(recipientCount);
		assertThat(bean.sent()).isEqualTo(sent);
		assertThat(bean.status()).isEqualTo(status);
		assertThat(bean.subject()).isEqualTo(subject);

	}

	@Test
	void toStatus() {
		final var successful = 45;
		final var unsuccessful = 67;

		final var bean = HistoryMapper.toStatus(successful, unsuccessful);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.successful()).isEqualTo(successful);
		assertThat(bean.unsuccessful()).isEqualTo(unsuccessful);
	}

	@Test
	void toUserBatchesPage1Limit1() {
		final var firstBatch = Batch.builder().build();
		final var secondBatch = Batch.builder().build();
		final var batches = List.of(firstBatch, secondBatch);

		final var page = PagingUtil.toPage(1, 1, batches);

		final var bean = HistoryMapper.toUserBatches(page, 1);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.metaData().getCount()).isEqualTo(1);
		assertThat(bean.metaData().getLimit()).isEqualTo(1);
		assertThat(bean.metaData().getPage()).isEqualTo(1);
		assertThat(bean.metaData().getTotalPages()).isEqualTo(2);
		assertThat(bean.metaData().getTotalRecords()).isEqualTo(2);

		assertThat(bean.batches()).asInstanceOf(LIST).hasSize(1).containsExactly(firstBatch);
	}

	@Test
	void toUserBatchesPage2Limit1() {
		final var firstBatch = Batch.builder().build();
		final var secondBatch = Batch.builder().build();
		final var batches = List.of(firstBatch, secondBatch);

		final var page = PagingUtil.toPage(2, 1, batches);

		final var bean = HistoryMapper.toUserBatches(page, 2);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.metaData().getCount()).isEqualTo(1);
		assertThat(bean.metaData().getLimit()).isEqualTo(1);
		assertThat(bean.metaData().getPage()).isEqualTo(2);
		assertThat(bean.metaData().getTotalPages()).isEqualTo(2);
		assertThat(bean.metaData().getTotalRecords()).isEqualTo(2);

		assertThat(bean.batches()).asInstanceOf(LIST).hasSize(1).containsExactly(secondBatch);
	}

	@Test
	void toUserBatchesPage1Limit2() {
		final var firstBatch = Batch.builder().build();
		final var secondBatch = Batch.builder().build();
		final var batches = List.of(firstBatch, secondBatch);

		final var page = PagingUtil.toPage(1, 2, batches);

		final var bean = HistoryMapper.toUserBatches(page, 1);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.metaData().getCount()).isEqualTo(2);
		assertThat(bean.metaData().getLimit()).isEqualTo(2);
		assertThat(bean.metaData().getPage()).isEqualTo(1);
		assertThat(bean.metaData().getTotalPages()).isEqualTo(1);
		assertThat(bean.metaData().getTotalRecords()).isEqualTo(2);

		assertThat(bean.batches()).asInstanceOf(LIST).hasSize(2).containsExactly(firstBatch, secondBatch);
	}

	@Test
	void toUserBatchesPage2Limit2() {
		final var firstBatch = Batch.builder().build();
		final var secondBatch = Batch.builder().build();
		final var batches = List.of(firstBatch, secondBatch);

		final var page = PagingUtil.toPage(2, 2, batches);

		final var bean = HistoryMapper.toUserBatches(page, 2);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.metaData().getCount()).isZero();
		assertThat(bean.metaData().getLimit()).isEqualTo(2);
		assertThat(bean.metaData().getPage()).isEqualTo(2);
		assertThat(bean.metaData().getTotalPages()).isEqualTo(1);
		assertThat(bean.metaData().getTotalRecords()).isEqualTo(2);

		assertThat(bean.batches()).asInstanceOf(LIST).isEmpty();
	}
}
