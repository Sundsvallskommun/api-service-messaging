package se.sundsvall.messaging.integration.emailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createEmailDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.emailsender.SendEmailRequest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationTest {

	@Mock
	private EmailSenderClient mockClient;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private EmailSenderIntegrationMapper mapper;

	@InjectMocks
	private EmailSenderIntegration integration;

	@Test
	void test_sendEmail() {
		var emailDto = createEmailDto();

		when(mockClient.sendEmail(any(String.class), any(SendEmailRequest.class)))
			.thenReturn(ResponseEntity.ok().build());

		integration.sendEmail("2281", emailDto);

		verify(mockClient, times(1)).sendEmail(any(String.class), any(SendEmailRequest.class));
	}

	@Test
	void test_sendEmail_whenExceptionIsThrownByClient() {
		var emailDto = createEmailDto();
		when(mockClient.sendEmail(any(String.class), any(SendEmailRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(Status.BAD_REQUEST)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendEmail("2281", emailDto))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST));
			});

		verify(mockClient, times(1)).sendEmail(any(String.class), any(SendEmailRequest.class));
	}

}
