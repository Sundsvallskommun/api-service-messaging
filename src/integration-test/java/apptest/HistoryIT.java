package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Disabled;
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
	@Disabled // TODO: This will be modified and activated in UF-10434
	void test5_userHistoricalMessages() {
		final var userId = "issuer1";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	@Disabled // TODO: This will be modified and activated in UF-10434
	void test6_userHistoricalMessagesNotFound() {
		final var userId = "nonExistingUser";
		setupCall()
			.withServicePath(USER_MESSAGES_PATH.formatted(userId))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
