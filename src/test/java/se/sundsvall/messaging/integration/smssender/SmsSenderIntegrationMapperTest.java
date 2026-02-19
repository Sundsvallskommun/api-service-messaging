package se.sundsvall.messaging.integration.smssender;

import generated.se.sundsvall.smssender.SendSmsRequest.PriorityEnum;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.sundsvall.messaging.api.model.request.Priority;

import static org.assertj.core.api.Assertions.assertThat;

class SmsSenderIntegrationMapperTest {

	private final SmsSenderIntegrationMapper mapper = new SmsSenderIntegrationMapper();

	@Test
	void test_toSendSmsRequest_whenSmsRequestIsNull() {
		assertThat(mapper.toSendSmsRequest(null)).isNull();
	}

	@Test
	void test_toSendSmsRequestFromDto() {
		final var dto = SmsDto.builder()
			.withSender("someName")
			.withMobileNumber("someMobileNumber")
			.withMessage("someMessage")
			.build();

		final var sendSmsRequest = mapper.toSendSmsRequest(dto);

		assertThat(sendSmsRequest.getSender().getName()).isEqualTo("someName");
		assertThat(sendSmsRequest.getMobileNumber()).isEqualTo("someMobileNumber");
		assertThat(sendSmsRequest.getMessage()).isEqualTo("someMessage");
		assertThat(sendSmsRequest.getPriority()).isEqualTo(PriorityEnum.NORMAL);
	}

	@ParameterizedTest
	@MethodSource("priorityArgumentProvider")
	void test_toSendSmsRequestFromDtoWithNormalPriority(Priority priority, PriorityEnum expectedPriorityEnum) {
		final var dto = SmsDto.builder()
			.withPriority(priority)
			.build();

		final var sendSmsRequest = mapper.toSendSmsRequest(dto);

		assertThat(sendSmsRequest.getPriority()).isEqualTo(expectedPriorityEnum);
	}

	private static Stream<Arguments> priorityArgumentProvider() {
		return Stream.of(
			Arguments.of(null, PriorityEnum.NORMAL),
			Arguments.of(Priority.NORMAL, PriorityEnum.NORMAL),
			Arguments.of(Priority.HIGH, PriorityEnum.HIGH));
	}
}
