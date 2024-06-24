package se.sundsvall.messaging.api;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.CONVERSATION_HISTORY_PATH;
import static se.sundsvall.messaging.Constants.DELIVERY_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_DEPARTMENTS_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_PATH;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.model.Statistics.Letter;
import se.sundsvall.messaging.model.Statistics.Message;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.StatisticsService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class StatusAndHistoryResourceTests {

	@MockBean
	private HistoryService mockHistoryService;

	@MockBean
	private StatisticsService mockStatisticsService;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void getConversationHistoryWithoutDates(final MessageType messageType) {
		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withMessageType(messageType)
			.build();
		when(mockHistoryService.getConversationHistory(any(), any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH).build(Map.of("partyId", partyId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(HistoryResponse::messageType).containsExactly(messageType);

		verify(mockHistoryService).getConversationHistory(partyId, null, null);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void getConversationHistoryWithDates(final MessageType messageType) {
		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var fromDate = LocalDate.now().minusDays(30);
		final var toDate = LocalDate.now().minusDays(15);
		final var history = History.builder()
			.withMessageType(messageType)
			.build();
		when(mockHistoryService.getConversationHistory(any(), any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH)
				.queryParam("from", fromDate)
				.queryParam("to", toDate)
				.build(Map.of("partyId", partyId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(HistoryResponse::messageType).containsExactly(messageType);

		verify(mockHistoryService).getConversationHistory(partyId, fromDate, toDate);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	void getConversationHistoryWhenNoHistoryExists() {
		// Arrange
		final var partyId = UUID.randomUUID().toString();
		when(mockHistoryService.getConversationHistory(any(), any(), any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH).build(Map.of("partyId", partyId)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getConversationHistory(partyId, null, null);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getDeliveryStatus() {
		// Arrange
		final var deliveryId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withDeliveryId("deliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByDeliveryId(any())).thenReturn(Optional.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(DELIVERY_STATUS_PATH).build(Map.of("deliveryId", deliveryId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(DeliveryResult.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).hasAllNullFieldsOrPropertiesExcept("deliveryId", "messageType", "status");
		assertThat(response.deliveryId()).isEqualTo(history.deliveryId());
		assertThat(response.messageType()).isEqualTo(history.messageType());
		assertThat(response.status()).isEqualTo(history.status());

		verify(mockHistoryService).getHistoryByDeliveryId(deliveryId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getDeliveryStatusWhenNoStatusExists() {
		// Arrange
		final var deliveryId = UUID.randomUUID().toString();

		when(mockHistoryService.getHistoryByDeliveryId(any())).thenReturn(Optional.empty());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(DELIVERY_STATUS_PATH).build(Map.of("deliveryId", deliveryId)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByDeliveryId(deliveryId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getBatchStatus() {
		// Arrange
		final var batchId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId(batchId)
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByBatchId(any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BATCH_STATUS_PATH).build(Map.of("batchId", batchId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MessageBatchResult.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(MessageBatchResult::batchId).containsExactly(batchId);

		verify(mockHistoryService).getHistoryByBatchId(batchId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getBatchStatusWhenNoHistoryExists() {
		// Arrange
		final var batchId = UUID.randomUUID().toString();
		when(mockHistoryService.getHistoryByBatchId(any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BATCH_STATUS_PATH).build(Map.of("batchId", batchId)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByBatchId(batchId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getMessageStatus() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId("someBatchId")
			.withMessageId(messageId)
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMessageId(any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_STATUS_PATH).build(Map.of("messageId", messageId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(MessageResult::messageId).containsExactly(messageId);

		verify(mockHistoryService).getHistoryByMessageId(messageId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getMessageStatusWhenNoHistoryExists() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		when(mockHistoryService.getHistoryByMessageId(any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_STATUS_PATH).build(Map.of("messageId", messageId)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByMessageId(messageId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);
	}

	@Test
	void getMessageAndDelivery() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId("someBatchId")
			.withMessageId(messageId)
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMessageId(any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_AND_DELIVERY_PATH).build(Map.of("messageId", messageId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.allSatisfy(hr -> {
				assertThat(hr.messageType()).isEqualTo(SMS);
				assertThat(hr.status()).isEqualTo(SENT);
			});

		verify(mockHistoryService).getHistoryByMessageId(messageId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);

	}

	@Test
	void getMessageAndDeliveryWhenNoHistoryExists() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		when(mockHistoryService.getHistoryByMessageId(any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_AND_DELIVERY_PATH).build(Map.of("messageId", messageId)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByMessageId(messageId);
		verifyNoMoreInteractions(mockHistoryService);
		verifyNoInteractions(mockStatisticsService);

	}

	@Test
	void getStatisticsWithMinimalParameterSettings() {
		// Arrange
		final var statistics = Statistics.builder()
			.withDigitalMail(Count.builder().withSent(1).withFailed(2).build())
			.withEmail(Count.builder().withSent(3).withFailed(4).build())
			.withLetter(Letter.builder().withDigitalMail(Count.builder().withSent(5).withFailed(6).build())
				.withSnailMail(Count.builder().withSent(7).withFailed(8).build())
				.build())
			.withMessage(Message.builder().withEmail(Count.builder().withSent(9).withFailed(8).build())
				.withSms(Count.builder().withSent(8).withFailed(7).build())
				.withUndeliverable(6)
				.build()).withSms(Count.builder().withSent(5).withFailed(4).build())
			.withSnailMail(Count.builder().withSent(4).withFailed(3).build())
			.withWebMessage(Count.builder().withSent(3).withFailed(2).build())
			.build();
		when(mockStatisticsService.getStatistics(any(), any(), any())).thenReturn(statistics);

		// Act
		final var response = webTestClient.get()
			.uri(STATISTICS_PATH)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Statistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(statistics);

		verify(mockStatisticsService).getStatistics(null, null, null);
		verifyNoMoreInteractions(mockStatisticsService);
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getStatisticsWithFullParameterSettings() {
		// Arrange
		final var messageType = MessageType.DIGITAL_MAIL;
		final var from = LocalDate.now().minusDays(2);
		final var to = LocalDate.now();
		final var statistics = Statistics.builder()
			.withDigitalMail(Count.builder().withSent(1).withFailed(2).build())
			.build();
		when(mockStatisticsService.getStatistics(any(), any(), any())).thenReturn(statistics);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path(STATISTICS_PATH)
				.queryParam("messageType", messageType)
				.queryParam("from", from)
				.queryParam("to", to)
				.build())
			.exchange()
			.expectStatus().isOk()
			.expectBody(Statistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(statistics);

		verify(mockStatisticsService).getStatistics(messageType, from, to);
		verifyNoMoreInteractions(mockStatisticsService);
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getStatisticsForAllDepartments() {
		// Arrange
		final var statistics = DepartmentStatistics.builder()
			.withOrigin("origin")
			.withDepartmentLetters(
				List.of(DepartmentLetter.builder()
					.withDepartment("department")
					.withDigitalMail(Count.builder().withSent(1).withFailed(1).build()).build())).build();
		when(mockStatisticsService.getDepartmentLetterStatistics(any(), any(), any(), any())).thenReturn(List.of(statistics));

		// Act
		final var response = webTestClient.get()
			.uri(STATISTICS_FOR_DEPARTMENTS_PATH)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DepartmentStatistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).containsExactly(statistics);

		verify(mockStatisticsService).getDepartmentLetterStatistics(null, null, null, null);
		verifyNoMoreInteractions(mockStatisticsService);
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getStatisticsForSpecificDepartmentWithMinimalParameterSettings() {
		// Arrange
		final var department = "department";
		final var statistics = DepartmentStatistics.builder()
			.withOrigin("origin")
			.withDepartmentLetters(
				List.of(DepartmentLetter.builder()
					.withDepartment(department)
					.withDigitalMail(Count.builder().withSent(1).withFailed(1).build()).build())).build();
		when(mockStatisticsService.getDepartmentLetterStatistics(any(), any(), any(), any())).thenReturn(List.of(statistics));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH).build(Map.of("department", department)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DepartmentStatistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).containsExactly(statistics);

		verify(mockStatisticsService).getDepartmentLetterStatistics(null, department, null, null);
		verifyNoMoreInteractions(mockStatisticsService);
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getStatisticsForSpecificDepartmentWithFullParameterSettings() {
		// Arrange
		final var department = "department";
		final var origin = "origin";
		final var from = LocalDate.now().minusDays(2);
		final var to = LocalDate.now();
		final var statistics = DepartmentStatistics.builder()
			.withOrigin(origin)
			.withDepartmentLetters(
				List.of(DepartmentLetter.builder()
					.withDepartment(department)
					.withDigitalMail(Count.builder().withSent(1).withFailed(1).build()).build())).build();
		when(mockStatisticsService.getDepartmentLetterStatistics(any(), any(), any(), any())).thenReturn(List.of(statistics));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH)
				.queryParam("origin", origin)
				.queryParam("from", from)
				.queryParam("to", to)
				.build(Map.of("department", department)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DepartmentStatistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).containsExactly(statistics);

		verify(mockStatisticsService).getDepartmentLetterStatistics(origin, department, from, to);
		verifyNoMoreInteractions(mockStatisticsService);
		verifyNoInteractions(mockHistoryService);
	}
}
