package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_DEPARTMENTS_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_PATH;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.service.StatisticsService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
public class StatisticsResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockBean
	private StatisticsService mockStatisticsService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getStatisticsWithMinimalParameterSettings() {
		// Arrange
		final var statistics = Statistics.builder()
			.withDigitalMail(Count.builder().withSent(1).withFailed(2).build())
			.withEmail(Count.builder().withSent(3).withFailed(4).build())
			.withLetter(Statistics.Letter.builder().withDigitalMail(Count.builder().withSent(5).withFailed(6).build())
				.withSnailMail(Count.builder().withSent(7).withFailed(8).build())
				.build())
			.withMessage(Statistics.Message.builder().withEmail(Count.builder().withSent(9).withFailed(8).build())
				.withSms(Count.builder().withSent(8).withFailed(7).build())
				.withUndeliverable(6)
				.build()).withSms(Count.builder().withSent(5).withFailed(4).build())
			.withSnailMail(Count.builder().withSent(4).withFailed(3).build())
			.withWebMessage(Count.builder().withSent(3).withFailed(2).build())
			.build();
		when(mockStatisticsService.getStatistics(any(), any(), any(), any())).thenReturn(statistics);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Statistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(statistics);

		verify(mockStatisticsService).getStatistics(null, null, null, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockStatisticsService);
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
		when(mockStatisticsService.getStatistics(any(), any(), any(), any())).thenReturn(statistics);

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path(STATISTICS_PATH)
				.queryParam("messageType", messageType)
				.queryParam("from", from)
				.queryParam("to", to)
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Statistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isEqualTo(statistics);

		verify(mockStatisticsService).getStatistics(messageType, from, to, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockStatisticsService);
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
		when(mockStatisticsService.getDepartmentLetterStatistics(any(), any(), any(), any(), any())).thenReturn(List.of(statistics));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_FOR_DEPARTMENTS_PATH).build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DepartmentStatistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).containsExactly(statistics);

		verify(mockStatisticsService).getDepartmentLetterStatistics(null, null, null, null, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockStatisticsService);
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
		when(mockStatisticsService.getDepartmentLetterStatistics(any(), any(), any(), any(), any())).thenReturn(List.of(statistics));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH).build(Map.of("department", department, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DepartmentStatistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).containsExactly(statistics);

		verify(mockStatisticsService).getDepartmentLetterStatistics(null, department, null, null, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockStatisticsService);
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
		when(mockStatisticsService.getDepartmentLetterStatistics(any(), any(), any(), any(), any())).thenReturn(List.of(statistics));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH)
				.queryParam("origin", origin)
				.queryParam("from", from)
				.queryParam("to", to)
				.build(Map.of("department", department, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(DepartmentStatistics.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).containsExactly(statistics);

		verify(mockStatisticsService).getDepartmentLetterStatistics(origin, department, from, to, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockStatisticsService);
	}
}
