package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.api.model.request.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.Header.REFERENCES;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class EmailRequestTests {

	@Test
	void testConstructorAndGetters() {
		final var externalReferences = List.of(new ExternalReference("someKey", "someValue"));
		final var party = new EmailRequest.Party("somePartyId", externalReferences);
		final var sender = new EmailRequest.Sender("someName", "someAddress", "someReplyTo");
		final var attachments = List.of(new EmailRequest.Attachment("someName", "someContentType", "someContent"));
		final var request = new EmailRequest(party, "someEmailAddress", "someSubject",
			"someMessage", "someHtmlMessage", sender, "someOrigin", attachments,
			Map.of(
				MESSAGE_ID, List.of("someMessageId"),
				REFERENCES, List.of("someReferences", "someMoreReferences"),
				IN_REPLY_TO, List.of("someInReplyTo")));

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.party()).satisfies(requestParty -> {
			assertThat(requestParty.partyId()).isEqualTo("somePartyId");
			assertThat(requestParty.externalReferences()).hasSize(1).element(0).satisfies(extRef -> {
				assertThat(extRef.key()).isEqualTo("someKey");
				assertThat(extRef.value()).isEqualTo("someValue");
			});
		});
		assertThat(request.sender()).satisfies(requestSender -> {
			assertThat(requestSender.name()).isEqualTo("someName");
			assertThat(requestSender.address()).isEqualTo("someAddress");
			assertThat(requestSender.replyTo()).isEqualTo("someReplyTo");
		});
		assertThat(request.message()).isEqualTo("someMessage");
		assertThat(request.htmlMessage()).isEqualTo("someHtmlMessage");
		assertThat(request.origin()).isEqualTo("someOrigin");
		assertThat(request.attachments()).hasSize(1).element(0).satisfies(attachment -> {
			assertThat(attachment.name()).isEqualTo("someName");
			assertThat(attachment.contentType()).isEqualTo("someContentType");
			assertThat(attachment.content()).isEqualTo("someContent");
		});
		assertThat(request.headers().get(MESSAGE_ID).getFirst()).isEqualTo("someMessageId");
		assertThat(request.headers().get(IN_REPLY_TO).getFirst()).isEqualTo("someInReplyTo");
		assertThat(request.headers().get(REFERENCES).getFirst()).isEqualTo("someReferences");
		assertThat(request.headers().get(REFERENCES)).hasSize(2);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmailRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}
