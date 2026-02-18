package se.sundsvall.messaging.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.request.Header;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.createEmailBatchRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestAttachment;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestParty;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestSender;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceEmailBatchFailureTest {

	private static final String URL = "/" + MUNICIPALITY_ID + "/email/batch";

	private static final EmailBatchRequest REQUEST = createEmailBatchRequest();

	private static final EmailBatchRequest.Party PARTY = createValidEmailBatchRequestParty();

	private static final EmailBatchRequest.Sender SENDER = createValidEmailBatchRequestSender();

	private static final EmailBatchRequest.Attachment ATTACHMENT = createValidEmailBatchRequestAttachment();

	@MockitoBean
	private MessageService messageServiceMock;

	@MockitoBean
	private MessageEventDispatcher eventDispatcherMock;

	@Autowired
	private WebTestClient webTestClient;

	private static Stream<Arguments> emailBatchRequestBadRequestProvider() {
		return Stream.of(
			Arguments.of(REQUEST.withSubject(null), "subject", "must not be blank"),
			Arguments.of(REQUEST.withSubject(""), "subject", "must not be blank"),
			Arguments.of(REQUEST.withHtmlMessage(""), "htmlMessage", "not a valid BASE64-encoded string"),
			Arguments.of(REQUEST.withHtmlMessage("not base64"), "htmlMessage", "not a valid BASE64-encoded string"),
			Arguments.of(REQUEST.withParties(List.of(PARTY.withEmailAddress(null))), "parties[0].emailAddress", "must not be blank"),
			Arguments.of(REQUEST.withParties(List.of(PARTY.withEmailAddress(""))), "parties[0].emailAddress", "must not be blank"),
			Arguments.of(REQUEST.withParties(List.of(PARTY.withEmailAddress("null"))), "parties[0].emailAddress", "must be a well-formed email address"),
			Arguments.of(REQUEST.withParties(List.of(PARTY.withPartyId("not uuid"))), "parties[0].partyId", "not a valid UUID"),
			Arguments.of(REQUEST.withSender(SENDER.withReplyTo("null")), "sender.replyTo", "must be a well-formed email address"),
			Arguments.of(REQUEST.withSender(SENDER.withName(null)), "sender.name", "must not be blank"),
			Arguments.of(REQUEST.withSender(SENDER.withName("")), "sender.name", "must not be blank"),
			Arguments.of(REQUEST.withSender(SENDER.withAddress(null)), "sender.address", "must not be blank"),
			Arguments.of(REQUEST.withSender(SENDER.withAddress("")), "sender.address", "must not be blank"),
			Arguments.of(REQUEST.withSender(SENDER.withAddress("not an email")), "sender.address", "must be a well-formed email address"),
			Arguments.of(REQUEST.withAttachments(List.of(ATTACHMENT.withContent(null))), "attachments[0].content", "not a valid BASE64-encoded string"),
			Arguments.of(REQUEST.withAttachments(List.of(ATTACHMENT.withContent("not base64"))), "attachments[0].content", "not a valid BASE64-encoded string"),
			Arguments.of(REQUEST.withAttachments(List.of(ATTACHMENT.withName(null))), "attachments[0].name", "must not be blank"),
			Arguments.of(REQUEST.withAttachments(List.of(ATTACHMENT.withName(""))), "attachments[0].name", "must not be blank"),
			Arguments.of(REQUEST.withHeaders(Map.of(Header.MESSAGE_ID.name(), List.of("not a valid message id"))), "headers.MESSAGE_ID", "must start with '<', contain '@' and end with '>'"),
			Arguments.of(REQUEST.withHeaders(Map.of(Header.IN_REPLY_TO.name(), List.of("not a valid in reply to"))), "headers.IN_REPLY_TO", "must start with '<', contain '@' and end with '>'"),
			Arguments.of(REQUEST.withHeaders(Map.of(Header.REFERENCES.name(), List.of("not a valid reference"))), "headers.REFERENCES", "must start with '<', contain '@' and end with '>'"),
			Arguments.of(REQUEST.withHeaders(Map.of(Header.AUTO_SUBMITTED.name(), List.of("not a valid auto-submitted value"))), "headers.AUTO_SUBMITTED", "must be equal to 'auto-generated'"));
	}

	@ParameterizedTest
	@MethodSource("emailBatchRequestBadRequestProvider")
	void sendBatch(final EmailBatchRequest request, final String field, final String message) {
		final var response = webTestClient.post()
			.uri(URL)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple(field, message));

		verifyNoInteractions(messageServiceMock, eventDispatcherMock);
	}
}
