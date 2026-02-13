package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.X_ORIGIN_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER_USER_NAME;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

@ExtendWith(MockitoExtension.class)
class SnailMailSenderIntegrationTest {

	@Mock
	private SnailMailSenderIntegrationMapper mockMapper;

	@Mock
	private SnailMailSenderClient mockClient;

	@InjectMocks
	private SnailMailSenderIntegration integration;

	private final SnailMailDto snailmailDto = SnailMailDto.builder().withOrigin(X_ORIGIN_HEADER_VALUE).withSentBy(X_SENT_BY_HEADER_USER_NAME).build();

	@Test
	void test_sendSnailmail() {
		when(mockMapper.toSendSnailmailRequest(any(SnailMailDto.class))).thenReturn(new SendSnailMailRequest());
		when(mockClient.sendSnailmail(eq(X_SENT_BY_HEADER_USER_NAME), eq(X_SENT_BY_HEADER_USER_NAME), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class)))
			.thenReturn(ResponseEntity.ok().build());

		integration.sendSnailMail(MUNICIPALITY_ID, snailmailDto);

		verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailMailDto.class));
		verify(mockClient, times(1)).sendSnailmail(eq(X_SENT_BY_HEADER_USER_NAME), eq(X_SENT_BY_HEADER_USER_NAME), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class));
		verifyNoMoreInteractions(mockMapper, mockClient);
	}

	@Test
	void test_sendSnailmail_whenExceptionIsThrownByClient() {
		when(mockMapper.toSendSnailmailRequest(any(SnailMailDto.class))).thenReturn(new SendSnailMailRequest());
		when(mockClient.sendSnailmail(eq(X_SENT_BY_HEADER_USER_NAME), eq(X_SENT_BY_HEADER_USER_NAME), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(HttpStatus.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(HttpStatus.BAD_REQUEST)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendSnailMail(MUNICIPALITY_ID, snailmailDto))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
				assertThat(problem.getCauseAsProblem()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
			});

		verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailMailDto.class));
		verify(mockClient, times(1)).sendSnailmail(eq(X_SENT_BY_HEADER_USER_NAME), eq(X_SENT_BY_HEADER_USER_NAME), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class));
		verifyNoMoreInteractions(mockMapper, mockClient);
	}
}
