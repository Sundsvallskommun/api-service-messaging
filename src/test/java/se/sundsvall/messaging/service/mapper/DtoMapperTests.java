package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.REFERENCES;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.configuration.Defaults;

@ExtendWith(MockitoExtension.class)
class DtoMapperTests {

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
		var emailRequest = createValidEmailRequest();

		var emailDto = dtoMapper.toEmailDto(emailRequest);

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
}
