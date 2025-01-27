package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.snailmail.SendSnailMailRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class SnailMailSenderIntegrationTest {

	@Mock
	private SnailMailSenderIntegrationMapper mockMapper;

	@Mock
	private SnailMailSenderClient mockClient;

	@InjectMocks
	private SnailMailSenderIntegration integration;

	private static final String X_ORIGIN_HEADER_VALUE = "origin";
	private static final String X_ISSUER_HEADER_VALUE = "issuer";
	private static final String MUNICIPALITY_ID = "2281";
	private final SnailMailDto snailmailDto = SnailMailDto.builder().withOrigin(X_ORIGIN_HEADER_VALUE).withIssuer(X_ISSUER_HEADER_VALUE).build();

	@Test
	void test_sendSnailmail() {
		when(mockMapper.toSendSnailmailRequest(any(SnailMailDto.class))).thenReturn(new SendSnailMailRequest());
		when(mockClient.sendSnailmail(eq(X_ISSUER_HEADER_VALUE), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class)))
			.thenReturn(ResponseEntity.ok().build());

		integration.sendSnailMail(MUNICIPALITY_ID, snailmailDto);

		verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailMailDto.class));
		verify(mockClient, times(1)).sendSnailmail(eq(X_ISSUER_HEADER_VALUE), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class));
		verifyNoMoreInteractions(mockMapper, mockClient);
	}

	@Test
	void test_sendSnailmail_whenExceptionIsThrownByClient() {
		when(mockMapper.toSendSnailmailRequest(any(SnailMailDto.class))).thenReturn(new SendSnailMailRequest());
		when(mockClient.sendSnailmail(eq(X_ISSUER_HEADER_VALUE), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(Status.BAD_REQUEST)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendSnailMail(MUNICIPALITY_ID, snailmailDto))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST));
			});

		verify(mockMapper, times(1)).toSendSnailmailRequest(any(SnailMailDto.class));
		verify(mockClient, times(1)).sendSnailmail(eq(X_ISSUER_HEADER_VALUE), eq(X_ORIGIN_HEADER_VALUE), eq(MUNICIPALITY_ID), any(SendSnailMailRequest.class));
		verifyNoMoreInteractions(mockMapper, mockClient);
	}
}
