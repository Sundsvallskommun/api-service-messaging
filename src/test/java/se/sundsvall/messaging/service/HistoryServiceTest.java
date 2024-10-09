package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createUserMessagesRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

	@Mock
	private DbIntegration mockDbIntegration;

	@InjectMocks
	private HistoryService historyService;

	@Test
	void test_getHistoryByMunicipalityIdAndMessageId() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndMessageId("2281", "someMessageId");

		assertThat(result).isNotEmpty();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndMessageId_whenNoEntityExists() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class)))
			.thenReturn(List.of());

		final var result = historyService.getHistoryByMunicipalityIdAndMessageId("2281", "someMessageId");

		assertThat(result).isEmpty();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndMessageId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByBatchId() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndBatchId(any(String.class), any(String.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndBatchId("2281", "someBatchId");

		assertThat(result).hasSize(1);

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndBatchId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndDeliveryId() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(History.builder().build()));

		final var result = historyService.getHistoryByMunicipalityIdAndDeliveryId("2281", "someBatchId");

		assertThat(result).isPresent();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class));
	}

	@Test
	void test_getHistoryByMunicipalityIdAndDeliveryId_whenNoEntityExists() {
		when(mockDbIntegration.getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		final var result = historyService.getHistoryByMunicipalityIdAndDeliveryId("2281", "someBatchId");

		assertThat(result).isEmpty();

		verify(mockDbIntegration, times(1)).getHistoryByMunicipalityIdAndDeliveryId(any(String.class), any(String.class));
	}

	@Test
	void test_getConversationHistory() {
		when(mockDbIntegration.getHistory(any(String.class), any(String.class), nullable(LocalDate.class), nullable(LocalDate.class)))
			.thenReturn(List.of(History.builder().build()));

		final var result = historyService.getConversationHistory("2281", "somePartyId", null, null);

		assertThat(result).hasSize(1);

		verify(mockDbIntegration, times(1))
			.getHistory(any(String.class), any(String.class), nullable(LocalDate.class), nullable(LocalDate.class));
	}

	@Test
	void getUserMessages() {
		var municipalityId = "2281";
		var request = createUserMessagesRequest();

		var result = historyService.getUserMessages(request, municipalityId);

		assertThat(result).isNull();
	}

	@Test
	void getAttachment() {
		var municipalityId = "2281";
		var messageId = "messageId";
		var fileName = "fileName";

		var result = historyService.getAttachment(municipalityId, messageId, fileName);

		assertThat(result).isNull();
	}

}
