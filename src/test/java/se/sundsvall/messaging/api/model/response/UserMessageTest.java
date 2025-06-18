package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.model.MessageStatus;

class UserMessageTest {

	private static final String MESSAGE_ID = "messageId";
	private static final String ISSUER = "issuer";
	private static final String ORIGIN = "origin";
	private static final LocalDateTime SENT = LocalDateTime.now();
	private static final List<UserMessage.Recipient> RECIPIENTS = List.of(UserMessage.Recipient.builder().build());
	private static final List<UserMessage.MessageAttachment> ATTACHMENTS = List.of(UserMessage.MessageAttachment.builder().build());
	private static final String PERSON_ID = "personId";
	private static final String MESSAGE_TYPE = "messageType";
	private static final String CONTENT_TYPE = "contentType";
	private static final String FILE_NAME = "fileName";
	private static final String SUBJECT = "subject";
	private static final UserMessage.Address ADDRESS = UserMessage.Address.builder().build();

	@Test
	void userMessageConstructor() {
		var bean = new UserMessage(MESSAGE_ID, ISSUER, ORIGIN, SENT, SUBJECT, RECIPIENTS, ATTACHMENTS);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.sent()).isEqualTo(SENT);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.recipients()).isEqualTo(RECIPIENTS);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean).hasOnlyFields("messageId", "issuer", "origin", "sent", "subject", "recipients", "attachments");
	}

	@Test
	void userMessageBuilder() {
		var bean = UserMessage.builder()
			.withMessageId(MESSAGE_ID)
			.withIssuer(ISSUER)
			.withOrigin(ORIGIN)
			.withSent(SENT)
			.withSubject(SUBJECT)
			.withRecipients(RECIPIENTS)
			.withAttachments(ATTACHMENTS)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.messageId()).isEqualTo(MESSAGE_ID);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.sent()).isEqualTo(SENT);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.recipients()).isEqualTo(RECIPIENTS);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean).hasOnlyFields("messageId", "issuer", "origin", "sent", "subject", "recipients", "attachments");
	}

	@Test
	void userMessageRecipientConstructor() {
		var bean = new UserMessage.Recipient(ADDRESS, PERSON_ID, MESSAGE_TYPE, MessageStatus.SENT.name());

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.personId()).isEqualTo(PERSON_ID);
		assertThat(bean.messageType()).isEqualTo(MESSAGE_TYPE);
		assertThat(bean.status()).isEqualTo(MessageStatus.SENT.name());
		assertThat(bean.address()).isEqualTo(ADDRESS);
		assertThat(bean).hasOnlyFields("personId", "messageType", "status", "address");
	}

	@Test
	void userMessageRecipientBuilder() {
		var bean = UserMessage.Recipient.builder()
			.withPersonId(PERSON_ID)
			.withMessageType(MESSAGE_TYPE)
			.withStatus(MessageStatus.SENT.name())
			.withAddress(ADDRESS)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.personId()).isEqualTo(PERSON_ID);
		assertThat(bean.messageType()).isEqualTo(MESSAGE_TYPE);
		assertThat(bean.status()).isEqualTo(MessageStatus.SENT.name());
		assertThat(bean.address()).isEqualTo(ADDRESS);
		assertThat(bean).hasOnlyFields("personId", "messageType", "status", "address");
	}

	@Test
	void userMessageAttachmentConstructor() {
		var bean = new UserMessage.MessageAttachment(CONTENT_TYPE, FILE_NAME);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.fileName()).isEqualTo(FILE_NAME);
		assertThat(bean).hasOnlyFields("contentType", "fileName");
	}

	@Test
	void userMessageAttachmentBuilder() {
		var bean = UserMessage.MessageAttachment.builder()
			.withContentType(CONTENT_TYPE)
			.withFileName(FILE_NAME)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.fileName()).isEqualTo(FILE_NAME);
		assertThat(bean).hasOnlyFields("contentType", "fileName");
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(UserMessage.builder().build()).hasAllNullFieldsOrPropertiesExcept();
		assertThat(UserMessage.Recipient.builder().build()).hasAllNullFieldsOrPropertiesExcept();
		assertThat(UserMessage.MessageAttachment.builder().build()).hasAllNullFieldsOrPropertiesExcept();
	}
}
