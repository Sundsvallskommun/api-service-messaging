package se.sundsvall.messaging.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createStatsProjection;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
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

	@Test
	void getMessageByDeliveryId() {
		when(mockMessageRepository.findByDeliveryId(any(String.class)))
			.thenReturn(Optional.of(MessageEntity.builder().build()));

		assertThat(dbIntegration.getMessageByDeliveryId("someDeliveryId")).isPresent();

		verify(mockMessageRepository).findByDeliveryId("someDeliveryId");
	}

	@Test
	void getLatestMessagesWithStatus() {
		when(mockMessageRepository.findByStatusOrderByCreatedAtAsc(any(MessageStatus.class)))
			.thenReturn(List.of(MessageEntity.builder().build(), MessageEntity.builder().build()));

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
		doNothing().when(mockMessageRepository).deleteByDeliveryId(any(String.class));

		dbIntegration.deleteMessageByDeliveryId("someDeliveryId");

		verify(mockMessageRepository).deleteByDeliveryId("someDeliveryId");
	}

	@Test
	void getHistoryByMessageId() {
		when(mockHistoryRepository.findByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistoryByMunicipalityIdAndMessageId("someMunicipalityId", "someMessageId")).hasSize(2);

		verify(mockHistoryRepository).findByMunicipalityIdAndMessageId("someMunicipalityId", "someMessageId");
	}

	@Test
	void getHistoryByBatchId() {
		when(mockHistoryRepository.findByMunicipalityIdAndBatchId(any(String.class), any(String.class)))
			.thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistoryByMunicipalityIdAndBatchId("someMunicipalityId", "someBatchId")).hasSize(2);

		verify(mockHistoryRepository).findByMunicipalityIdAndBatchId("someMunicipalityId", "someBatchId");
	}

	@Test
	void getHistoryByDeliveryId() {
		when(mockHistoryRepository.findByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(HistoryEntity.builder().build()));

		assertThat(dbIntegration.getHistoryByMunicipalityIdAndDeliveryId("someMunicipalityId", "somDeliveryId")).contains(History.builder().build());

		verify(mockHistoryRepository).findByMunicipalityIdAndDeliveryId("someMunicipalityId", "somDeliveryId");
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

		verify(mockHistoryRepository)
			.findAll(ArgumentMatchers.<Specification<HistoryEntity>>any());
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

		final var statsProjection = createStatsProjection(SNAIL_MAIL, LETTER, SENT, null, null, "2281");

		when(mockStatisticsRepository.findAllByParameters(municipalityId, department, origin, messageTypes, from, to))
			.thenReturn(List.of(statsProjection));

		var result = dbIntegration.getStatsByParameters(municipalityId, department, origin, messageTypes, from, to);

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
		verifyNoMoreInteractions(mockHistoryRepository);
	}

}
