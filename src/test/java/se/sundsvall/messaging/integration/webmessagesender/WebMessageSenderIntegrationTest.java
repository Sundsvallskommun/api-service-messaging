package se.sundsvall.messaging.integration.webmessagesender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class WebMessageSenderIntegrationTest {

	@Mock
	private WebMessageSenderClient mockClient;

	@Mock
	private WebMessageSenderIntegrationMapper mockMapper;

	private WebMessageSenderIntegration integration;

	@BeforeEach
	void setUp() {
		integration = new WebMessageSenderIntegration(mockClient, mockMapper);
	}

	@Test
	void test_sendWebMessage() {
		when(mockMapper.toCreateWebMessageRequest(any(WebMessageDto.class))).thenReturn(new CreateWebMessageRequest());
		when(mockClient.sendWebMessage(any(String.class), any(CreateWebMessageRequest.class)))
			.thenReturn(ResponseEntity.ok().build());

		integration.sendWebMessage("2281", WebMessageDto.builder().build());

		verify(mockMapper, times(1)).toCreateWebMessageRequest(any(WebMessageDto.class));
		verify(mockClient, times(1)).sendWebMessage(any(String.class), any(CreateWebMessageRequest.class));
	}

	@Test
	void test_sendWebMessage_whenExceptionIsThrownByClient() {
		when(mockMapper.toCreateWebMessageRequest(any(WebMessageDto.class))).thenReturn(new CreateWebMessageRequest());
		when(mockClient.sendWebMessage(any(String.class), any(CreateWebMessageRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(Status.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(Status.BAD_REQUEST)
					.build())
				.build());

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendWebMessage("2281", WebMessageDto.builder().build()))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(Status.BAD_GATEWAY);
				assertThat(problem.getCause()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(Status.BAD_REQUEST));
			});

		verify(mockMapper, times(1)).toCreateWebMessageRequest(any(WebMessageDto.class));
		verify(mockClient, times(1)).sendWebMessage(any(String.class), any(CreateWebMessageRequest.class));
	}

}
