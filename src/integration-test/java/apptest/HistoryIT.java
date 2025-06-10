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
	private static final String MESSAGE_AND_DELIVERY_PATH = "/" + MUNICIPALITY_ID + "/messages/";
	private static final String MESSAGE_AND_DELIVERY_METADATA_PATH = "/" + MUNICIPALITY_ID + "/messages/%s/metadata";
	private static final String USER_MESSAGES_PATH = "/" + MUNICIPALITY_ID + "/users/%s/messages";
	private static final String USER_MESSAGE_PATH = "/" + MUNICIPALITY_ID + "/users/%s/messages/%s";
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
		final var userId = "issuer1";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId) + "?page=1&limit=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_userHistoricalMessagesNotFound() {
		final var userId = "nonExistingUser";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId) + "?page=1&limit=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test7_readAttachment() throws IOException {
		final var messageId = "0c803a34-9418-48a8-a058-10e8394116b8";
		final var fileName = "smiley.jpg";

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

	@Test
	void test9_messageMetadata() {
		final var messageId = "d4545198-3356-40a8-83a9-5950e74585a6";
		setupCall()
			.withServicePath(MESSAGE_AND_DELIVERY_METADATA_PATH.formatted(messageId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test10_messageMetadataNotFound() {
		final var messageId = "5b79fd71-c4fc-4f78-a5f4-15b070fbc51f";
		setupCall()
			.withServicePath(MESSAGE_AND_DELIVERY_METADATA_PATH.formatted(messageId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test11_userMessage() {
		final var userId = "issuer123";
		final var messageId = "d5161acb-2462-4065-a679-53b1cd77be92";
		setupCall()
			.withServicePath(USER_MESSAGE_PATH.formatted(userId, messageId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test12_userMessageNotFound() {
		// messageId exists but not for the userId/issuer, should be a 404
		final var userId = "not-found";
		final var messageId = "d5161acb-2462-4065-a679-53b1cd77be92";
		setupCall()
			.withServicePath(USER_MESSAGE_PATH.formatted(userId, messageId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test13_userMessageNoAttachment() {
		final var userId = "issuer123";
		final var messageId = "ed0e2373-b15a-4261-86eb-b3c0ecedfdb8";
		setupCall()
			.withServicePath(USER_MESSAGE_PATH.formatted(userId, messageId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test14_batchFilteredUserHistoricalMessages() {
		final var userId = "issuer1";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId) + "?batchId=acf4b29e-8405-424c-afea-1d347a96a6aa&page=1&limit=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test15_userHistoricalMessagesWhenAllFailed() {
		final var userId = "issuer2";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId) + "?page=1&limit=10")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
