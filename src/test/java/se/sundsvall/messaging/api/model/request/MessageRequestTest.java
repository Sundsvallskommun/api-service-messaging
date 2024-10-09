package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.model.request.MessageRequest.Message;
import se.sundsvall.messaging.api.model.request.MessageRequest.Message.Party;
import se.sundsvall.messaging.api.model.request.MessageRequest.Message.Sender;
import se.sundsvall.messaging.api.model.request.MessageRequest.Message.Sender.Email;
import se.sundsvall.messaging.api.model.request.MessageRequest.Message.Sender.Sms;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageRequestTest {

	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final List<Message> MESSAGES = List.of(Message.builder().build());
	private static final Party PARTY = Party.builder().build();
	private static final Map<String, List<String>> FILTERS = Map.of("key", List.of("value"));
	private static final Sender SENDER = Sender.builder().build();
	private static final String SUBJECT = "subject";
	private static final String MESSAGE = "message";
	private static final String HTML_MESSAGE = "htmlMessage";
	private static final String PARTY_ID = "partyId";
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final Email EMAIL = Email.builder().build();
	private static final Sms SMS = Sms.builder().build();
	private static final String EMAIL_NAME = "emailName";
	private static final String ADDRESS = "address";
	private static final String REPLY_TO = "replyTo";
	private static final String SMS_NAME = "smsName";

	// MessageRequest
	@Test
	void testMessageRequestConstructor() {
		final var bean = new MessageRequest(ORIGIN, ISSUER, MESSAGES);

		assertMessageRequest(bean);
	}

	@Test
	void testMessageRequestBuilder() {
		final var bean = MessageRequest.builder()
			.withIssuer(ISSUER)
			.withMessages(MESSAGES)
			.withOrigin(ORIGIN)
			.build();

		assertMessageRequest(bean);
	}

	private void assertMessageRequest(final MessageRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.messages()).isEqualTo(MESSAGES);
	}

	// MessageRequest.Message
	@Test
	void testMessageRequestMessageConstructor() {
		final var bean = new MessageRequest.Message(PARTY, FILTERS, SENDER, SUBJECT, MESSAGE, HTML_MESSAGE);

		assertMessageRequestMessage(bean);
	}

	@Test
	void testMessageRequestMessageBuilder() {
		final var bean = MessageRequest.Message.builder()
			.withFilters(FILTERS)
			.withHtmlMessage(HTML_MESSAGE)
			.withMessage(MESSAGE)
			.withParty(PARTY)
			.withSender(SENDER)
			.withSubject(SUBJECT)
			.build();

		assertMessageRequestMessage(bean);
	}

	private void assertMessageRequestMessage(final Message bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.filters()).isEqualTo(FILTERS);
		assertThat(bean.htmlMessage()).isEqualTo(HTML_MESSAGE);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
	}

	// MessageRequest.Message.Party
	@Test
	void testMessageRequestMessagePartyConstructor() {
		final var bean = new MessageRequest.Message.Party(PARTY_ID, EXTERNAL_REFERENCES);

		assertMessageRequestMessageParty(bean);
	}

	@Test
	void testMessageRequestMessagePartyBuilder() {
		final var bean = MessageRequest.Message.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyId(PARTY_ID)
			.build();

		assertMessageRequestMessageParty(bean);
	}

	private void assertMessageRequestMessageParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// MessageRequest.Message.Sender
	@Test
	void testMessageRequestMessageSenderConstructor() {
		final var bean = new MessageRequest.Message.Sender(EMAIL, SMS);

		assertMessageRequestMessageSender(bean);
	}

	@Test
	void testMessageRequestMessageSenderBuilder() {
		final var bean = MessageRequest.Message.Sender.builder()
			.withEmail(EMAIL)
			.withSms(SMS)
			.build();

		assertMessageRequestMessageSender(bean);
	}

	private void assertMessageRequestMessageSender(final Sender bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.email()).isEqualTo(EMAIL);
		assertThat(bean.sms()).isEqualTo(SMS);
	}

	// MessageRequest.Message.Sender.Email
	@Test
	void testMessageRequestMessageSenderEmailConstructor() {
		final var bean = new MessageRequest.Message.Sender.Email(EMAIL_NAME, ADDRESS, REPLY_TO);

		assertMessageRequestMessageSenderEmail(bean);
	}

	@Test
	void testMessageRequestMessageSenderEmailBuilder() {
		final var bean = MessageRequest.Message.Sender.Email.builder()
			.withAddress(ADDRESS)
			.withName(EMAIL_NAME)
			.withReplyTo(REPLY_TO)
			.build();

		assertMessageRequestMessageSenderEmail(bean);
	}

	private void assertMessageRequestMessageSenderEmail(final Email bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.address()).isEqualTo(ADDRESS);
		assertThat(bean.name()).isEqualTo(EMAIL_NAME);
		assertThat(bean.replyTo()).isEqualTo(REPLY_TO);
	}

	// MessageRequest.Message.Sender.Sms
	@Test
	void testMessageRequestMessageSenderSmsConstructor() {
		final var bean = new MessageRequest.Message.Sender.Sms(SMS_NAME);

		assertMessageRequestMessageSenderSms(bean);
	}

	@Test
	void testMessageRequestMessageSenderSmsBuilder() {
		final var bean = MessageRequest.Message.Sender.Sms.builder()
			.withName(SMS_NAME)
			.build();

		assertMessageRequestMessageSenderSms(bean);
	}

	private void assertMessageRequestMessageSenderSms(final Sms bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.name()).isEqualTo(SMS_NAME);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(MessageRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(MessageRequest.Message.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(MessageRequest.Message.Party.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(MessageRequest.Message.Sender.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(MessageRequest.Message.Sender.Email.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(MessageRequest.Message.Sender.Sms.builder().build()).hasAllNullFieldsOrProperties();
	}
}
