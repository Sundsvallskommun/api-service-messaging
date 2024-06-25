package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.api.model.request.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.Header.REFERENCES;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.configuration.Defaults;

class DtoMapperTest {

	private DtoMapper dtoMapper;

	@BeforeEach
	void setUp() {
		dtoMapper = new DtoMapper(
			new Defaults(
				new Defaults.Sms("name"),
				new Defaults.Email("name", "address", "replyTo"),
				new Defaults.DigitalMail("municipalityId",
					new Defaults.DigitalMail.SupportInfo("text", "emailAddress", "phoneNumber", "url"), "subject")));
	}

	@Test
	void toEmailDtoTest() {
		final var emailRequest = createValidEmailRequest();

		final var emailDto = dtoMapper.toEmailDto(emailRequest);

		assertThat(emailDto.emailAddress()).isEqualTo(emailRequest.emailAddress());
		assertThat(emailDto.sender().name()).isEqualTo(emailRequest.sender().name());
		assertThat(emailDto.sender().address()).isEqualTo(emailRequest.sender().address());
		assertThat(emailDto.sender().replyTo()).isEqualTo(emailRequest.sender().replyTo());
		assertThat(emailDto.subject()).isEqualTo(emailRequest.subject());
		assertThat(emailDto.message()).isEqualTo(emailRequest.message());
		assertThat(emailDto.attachments()).hasSize(emailRequest.attachments().size());
		assertThat(emailDto.headers().get("MESSAGE_ID")).containsExactlyInAnyOrderElementsOf(emailRequest.headers().get(MESSAGE_ID));
		assertThat(emailDto.headers().get("IN_REPLY_TO")).containsExactlyInAnyOrderElementsOf(emailRequest.headers().get(IN_REPLY_TO));
		assertThat(emailDto.headers().get("REFERENCES")).containsExactlyInAnyOrderElementsOf(emailRequest.headers().get(REFERENCES));

	}

	@Test
	void toSmsDtoTest() {
		final var smsRequest = createValidSmsRequest();

		final var smsDto = dtoMapper.toSmsDto(smsRequest);

		assertThat(smsDto.message()).isEqualTo(smsRequest.message());
		assertThat(smsDto.mobileNumber()).isEqualTo(smsRequest.mobileNumber());
		assertThat(smsDto.priority()).isEqualTo(smsRequest.priority());
		assertThat(smsDto.sender()).isEqualTo(smsRequest.sender());
	}

	@Test
	void toSmsDtoWithoutSetPriorityTest() {
		final var smsRequest = createValidSmsRequest()
			.withPriority(null);

		final var smsDto = dtoMapper.toSmsDto(smsRequest);

		assertThat(smsDto.message()).isEqualTo(smsRequest.message());
		assertThat(smsDto.mobileNumber()).isEqualTo(smsRequest.mobileNumber());
		assertThat(smsDto.priority()).isEqualTo(Priority.NORMAL);
		assertThat(smsDto.sender()).isEqualTo(smsRequest.sender());
	}
}
