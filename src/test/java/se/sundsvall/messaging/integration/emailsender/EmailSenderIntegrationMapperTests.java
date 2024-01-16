package se.sundsvall.messaging.integration.emailsender;


import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createEmailDto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class EmailSenderIntegrationMapperTests {

	@Test
	void test_toSendEmailRequest_whenRequestIsNull() {
		assertThat(EmailSenderIntegrationMapper.toSendEmailRequest(null)).isNull();
	}

	@Test
	void test_toSendEmailRequest() {
		var dto = createEmailDto();

		var mappedRequest = EmailSenderIntegrationMapper.toSendEmailRequest(dto);

		assertThat(mappedRequest.getSender().getName()).isEqualTo("someSender");
		assertThat(mappedRequest.getSender().getAddress()).isEqualTo("noreply@somehost.com");
		assertThat(mappedRequest.getSender().getReplyTo()).isEqualTo("someReplyTo");
		assertThat(mappedRequest.getEmailAddress()).isEqualTo("someone@somehost.com");
		assertThat(mappedRequest.getSubject()).isEqualTo("someSubject");
		assertThat(mappedRequest.getMessage()).isEqualTo("someMessage");
		assertThat(mappedRequest.getHtmlMessage()).isEqualTo("someHtmlMessage");
		assertThat(mappedRequest.getAttachments()).hasSize(1);
		assertThat(mappedRequest.getHeaders().get("MESSAGE_ID").getFirst()).isEqualTo("someMessageId");
		assertThat(mappedRequest.getHeaders().get("IN_REPLY_TO").getFirst()).isEqualTo("someInReplyTo");
		assertThat(mappedRequest.getHeaders().get("REFERENCES")).containsExactlyInAnyOrder("someReferences", "someMoreReferences");
	}
}
