package se.sundsvall.messaging.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.messaging.TestDataFactory.createStatisticsEntity;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.projection.MessageIdProjection;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DbIntegrationTest {

	@Mock
	private MessageRepository mockMessageRepository;

	@Mock
	private HistoryRepository mockHistoryRepository;

	@Mock
	private StatisticsRepository mockStatisticsRepository;

	@InjectMocks
	private DbIntegration dbIntegration;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(mockMessageRepository, mockHistoryRepository, mockStatisticsRepository);
	}

	@Test
	void getMessageByDeliveryId() {
		final var deliveryId = "deliveryId";
		when(mockMessageRepository.findByDeliveryId(deliveryId)).thenReturn(Optional.of(MessageEntity.builder().build()));

		assertThat(dbIntegration.getMessageByDeliveryId(deliveryId)).isPresent();

		verify(mockMessageRepository).findByDeliveryId(deliveryId);
	}

	@Test
	void getLatestMessagesWithStatus() {
		when(mockMessageRepository.findByStatusOrderByCreatedAtAsc(PENDING)).thenReturn(List.of(MessageEntity.builder().build(), MessageEntity.builder().build()));

		assertThat(dbIntegration.getLatestMessagesWithStatus(PENDING)).hasSize(2);

		verify(mockMessageRepository).findByStatusOrderByCreatedAtAsc(PENDING);
	}

	@Test
	void saveMessage() {
		when(mockMessageRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder().build());

		assertThat(dbIntegration.saveMessage(Message.builder().build())).isNotNull();

		verify(mockMessageRepository).save(any(MessageEntity.class));
	}

	@Test
	void saveMessages() {
		when(mockMessageRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder().build());

		assertThat(dbIntegration.saveMessages(List.of(Message.builder().build(), Message.builder().build()))).hasSize(2);

		verify(mockMessageRepository, times(2)).save(any(MessageEntity.class));
	}

	@Test
	void deleteMessageByDeliveryId() {
		final var deliveryId = "deliveryId";

		dbIntegration.deleteMessageByDeliveryId(deliveryId);

		verify(mockMessageRepository).deleteByDeliveryId(deliveryId);
	}

	@Test
	void getHistoryByMessageId() {
		final var municipalityId = "municipalityId";
		final var messageId = "messageId";
		when(mockHistoryRepository.findByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId)).hasSize(2);

		verify(mockHistoryRepository).findByMunicipalityIdAndMessageId(municipalityId, messageId);
	}

	@Test
	void getHistoryByBatchId() {
		final var municipalityId = "municipalityId";
		final var batchId = "batchId";
		when(mockHistoryRepository.findByMunicipalityIdAndBatchId(municipalityId, batchId)).thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistoryByMunicipalityIdAndBatchId(municipalityId, batchId)).hasSize(2);

		verify(mockHistoryRepository).findByMunicipalityIdAndBatchId(municipalityId, batchId);
	}

	@Test
	void getHistoryByDeliveryId() {
		final var municipalityId = "municipalityId";
		final var deliveryId = "deliveryId";
		when(mockHistoryRepository.findByMunicipalityIdAndDeliveryId(municipalityId, deliveryId)).thenReturn(Optional.of(HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistoryByMunicipalityIdAndDeliveryId(municipalityId, deliveryId)).contains(History.builder().build());

		verify(mockHistoryRepository).findByMunicipalityIdAndDeliveryId(municipalityId, deliveryId);
	}

	@Test
	void getHistoryWithNullValues() {
		when(mockHistoryRepository.findAll(ArgumentMatchers.<Specification<HistoryEntity>>any()))
			.thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistory(null, null, null, null)).hasSize(2);

		verify(mockHistoryRepository).findAll(ArgumentMatchers.<Specification<HistoryEntity>>any());
	}

	@Test
	void getHistory() {
		final var municipalityId = "municipalityId";
		final var partyId = "partyId";
		final var from = LocalDate.now().minusDays(1);
		final var to = LocalDate.now();
		when(mockHistoryRepository.findAll(ArgumentMatchers.<Specification<HistoryEntity>>any()))
			.thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistory(municipalityId, partyId, from, to)).hasSize(2);

		verify(mockHistoryRepository).findAll(ArgumentMatchers.<Specification<HistoryEntity>>any());
	}

	@Test
	void saveHistory() {
		when(mockHistoryRepository.save(any(HistoryEntity.class))).thenReturn(HistoryEntity.builder().build());

		assertThat(dbIntegration.saveHistory(Message.builder().build(), null)).isNotNull();

		verify(mockHistoryRepository).save(any(HistoryEntity.class));
	}

	@Test
	void getStatsByParameters() {
		final var municipalityId = "municipalityId";
		final var department = "department";
		final var origin = "origin";
		final var messageTypes = List.of(LETTER, SMS);
		final var from = LocalDate.now().minusDays(1);
		final var to = LocalDate.now();

		final var statsProjection = createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, null, null, "2281");

		when(mockStatisticsRepository.findAllByParameters(municipalityId, department, origin, messageTypes, from, to))
			.thenReturn(List.of(statsProjection));

		final var result = dbIntegration.getStatsByParameters(municipalityId, department, origin, messageTypes, from, to);

		assertThat(result).isNotEmpty().hasSize(1).allSatisfy(entry -> assertThat(entry).isEqualTo(statsProjection));
		verify(mockStatisticsRepository).findAllByParameters(municipalityId, department, origin, messageTypes, from, to);
	}

	@Test
	void existsByMunicipalityIdAndMessageIdAndIssuer() {
		final var municipalityId = "municipalityId";
		final var messageId = "messageId";
		final var issuer = "issuer";

		when(mockHistoryRepository.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).thenReturn(true);

		assertThat(dbIntegration.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)).isTrue();

		verify(mockHistoryRepository).existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer);
	}

	@Test
	void getUniqueMessageIdsForUserId() {
		final var municipalityId = "municipalityId";
		final var issuer = "issuer";
		final var date = LocalDateTime.now();
		final var pageRequest = PageRequest.of(12, 34);
		final Page<MessageIdProjection> result = Page.empty();

		when(mockHistoryRepository.findDistinctMessageIdsByMunicipalityIdAndIssuerAndCreatedAtIsAfter(municipalityId, issuer, date, pageRequest)).thenReturn(result);

		assertThat(dbIntegration.getUniqueMessageIds(municipalityId, issuer, date, pageRequest)).isEqualTo(result);

		verify(mockHistoryRepository).findDistinctMessageIdsByMunicipalityIdAndIssuerAndCreatedAtIsAfter(municipalityId, issuer, date, pageRequest);
	}

	@Test
	void getUniqueMessageIdsForUserIdAndBatch() {
		final var municipalityId = "municipalityId";
		final var batchId = "batchId";
		final var issuer = "issuer";
		final var date = LocalDateTime.now();
		final var pageRequest = PageRequest.of(12, 34);
		final Page<MessageIdProjection> result = Page.empty();

		when(mockHistoryRepository.findDistinctMessageIdsByMunicipalityIdAndBatchIdAndIssuerAndCreatedAtIsAfter(municipalityId, batchId, issuer, date, pageRequest)).thenReturn(result);

		assertThat(dbIntegration.getUniqueMessageIds(municipalityId, batchId, issuer, date, pageRequest)).isEqualTo(result);

		verify(mockHistoryRepository).findDistinctMessageIdsByMunicipalityIdAndBatchIdAndIssuerAndCreatedAtIsAfter(municipalityId, batchId, issuer, date, pageRequest);
	}

	@Test
	void getHistoryEntityByMunicipalityIdAndMessageId() {
		final var municipalityId = "municipalityId";
		final var messageId = "messageId";
		final var response = List.of(
			HistoryEntity.builder().withId(123L).build(),
			HistoryEntity.builder().withId(234L).build());

		when(mockHistoryRepository.findByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(response);

		assertThat(dbIntegration.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).isEqualTo(response);

		verify(mockHistoryRepository).findByMunicipalityIdAndMessageId(municipalityId, messageId);
	}

	@Test
	void getFirstHistoryEntityByMunicipalityIdAndMessageId() {
		final var municipalityId = "municipalityId";
		final var messageId = "messageId";
		final var historyEntity = HistoryEntity.builder().withId(556L).build();

		when(mockHistoryRepository.findFirstByMunicipalityIdAndMessageId(municipalityId, messageId)).thenReturn(Optional.of(historyEntity));

		assertThat(dbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId)).isEqualTo(historyEntity);

		verify(mockHistoryRepository).findFirstByMunicipalityIdAndMessageId(municipalityId, messageId);
	}

	@Test
	void getFirstHistoryEntityByMunicipalityIdAndMessageIdNotFound() {
		final var municipalityId = "municipalityId";
		final var messageId = "messageId";

		final var e = assertThrows(ThrowableProblem.class, () -> dbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId));

		assertThat(e.getStatus()).isEqualTo(NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: No history found for message id messageId");

		verify(mockHistoryRepository).findFirstByMunicipalityIdAndMessageId(municipalityId, messageId);
		verifyNoMoreInteractions(mockHistoryRepository);
	}
}
