package apptest;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/HistoryIT/", classes = Application.class)
class HistoryIT extends AbstractMessagingAppTest {

	private static final String CONVERSATION_HISTORY_PATH = "/" + MUNICIPALITY_ID + "/conversation-history/";
	private static final String MESSAGE_AND_DELIVERY_PATH = "/" + MUNICIPALITY_ID + "/message/";
	private static final String USER_MESSAGES_PATH = "/" + MUNICIPALITY_ID + "/users/%s/messages";
	private static final String MESSAGE_ATTACHMENT_PATH = "/" + MUNICIPALITY_ID + "/messages/%s/attachments/%s";

	@Test
	void test1_conversationHistory() {
		final var partyId = "d1d87cda-0dc5-41bb-9dd9-fa6ed2fd52ee";
		setupCall()
			.withServicePath(CONVERSATION_HISTORY_PATH + partyId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_conversationHistoryNotFound() {
		final var partyId = "54b91773-4c53-4b44-b6cf-a99231dc2fda";
		setupCall()
			.withServicePath(CONVERSATION_HISTORY_PATH + partyId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_messageHistoryAndDelivery() {
		final var messageId = "8fffe36f-be9d-42b9-a676-24244877c5ae";
		setupCall()
			.withServicePath(MESSAGE_AND_DELIVERY_PATH + messageId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_messageHistoryAndDeliveryNotFound() {
		final var messageId = "54b91773-4c53-4b44-b6cf-a99231dc2fda";
		setupCall()
			.withServicePath(MESSAGE_AND_DELIVERY_PATH + messageId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_userHistoricalMessages() {
		var userId = "issuer1";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId) + "?page=1&limit=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_userHistoricalMessagesNotFound() {
		var userId = "nonExistingUser";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId) + "?page=1&limit=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_readAttachment() throws IOException {
		var messageId = "0c803a34-9418-48a8-a058-10e8394116b8";
		var fileName = "smiley.jpg";

		setupCall()
			.withServicePath(MESSAGE_ATTACHMENT_PATH.formatted(messageId, fileName))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of("image/jpeg"))
			.withExpectedBinaryResponse(fileName)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test8_readAttachmentNotFound() {
		final var messageId = "67db7260-5829-40f4-ab3d-0ff55d117fb0";
		final var fileName = "doesNotExist.jpg";
		setupCall()
			.withServicePath(MESSAGE_ATTACHMENT_PATH.formatted(messageId, fileName))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
