package se.sundsvall.messaging.integration.smssender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.smssender.SendSmsRequest;
import generated.se.sundsvall.smssender.SendSmsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

@ExtendWith(MockitoExtension.class)
class SmsSenderIntegrationTest {

	@Mock
	private SmsSenderIntegrationMapper mockMapper;

	@Mock
	private SmsSenderClient mockClient;

	private SmsSenderIntegration integration;

	@BeforeEach
	void setUp() {
		integration = new SmsSenderIntegration(mockClient, mockMapper);
	}

	@Test
	void test_sendSms() {
		when(mockMapper.toSendSmsRequest(any(SmsDto.class))).thenReturn(new SendSmsRequest());
		when(mockClient.sendSms(anyString(), any(SendSmsRequest.class)))
			.thenReturn(ResponseEntity.ok(new SendSmsResponse().sent(true)));

		integration.sendSms("2281", SmsDto.builder().build());

		verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
		verify(mockClient, times(1)).sendSms(anyString(), any(SendSmsRequest.class));
	}

	@Test
	void test_sendSms_whenExceptionIsThrownByClient() {
		when(mockMapper.toSendSmsRequest(any(SmsDto.class))).thenReturn(new SendSmsRequest());
		when(mockClient.sendSms(anyString(), any(SendSmsRequest.class)))
			.thenThrow(Problem.builder()
				.withStatus(HttpStatus.BAD_GATEWAY)
				.withCause(Problem.builder()
					.withStatus(HttpStatus.BAD_REQUEST)
					.build())
				.build());

		var dto = SmsDto.builder().build();
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> integration.sendSms("2281", dto))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY);
				assertThat(problem.getCauseAsProblem()).isNotNull().satisfies(cause -> assertThat(cause.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
			});

		verify(mockMapper, times(1)).toSendSmsRequest(any(SmsDto.class));
		verify(mockClient, times(1)).sendSms(anyString(), any(SendSmsRequest.class));
	}

}
