package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;

@WireMockAppTestSuite(files = "classpath:/StatusIT/", classes = Application.class)
class StatusIT extends AbstractMessagingAppTest {

	private static final String BATCH_STATUS_PATH = "/" + MUNICIPALITY_ID + "/status/batch/";
	private static final String MESSAGE_STATUS_PATH = "/" + MUNICIPALITY_ID + "/status/messages/";
	private static final String DELIVERY_STATUS_PATH = "/" + MUNICIPALITY_ID + "/status/delivery/";

	@Test
	void test1_batchStatus() {
		final var batchId = "0082d48f-21ad-4cbf-b616-5ef84b885a3d";
		setupCall()
			.withServicePath(BATCH_STATUS_PATH + batchId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_batchStatusNotFound() {
		final var batchId = "787a8826-fdad-472a-834a-7ad7590e7f83";
		setupCall()
			.withServicePath(BATCH_STATUS_PATH + batchId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_messageStatus() {
		final var messageId = "5ee0274e-784a-4ffb-90b9-d2974b94878b";
		setupCall()
			.withServicePath(MESSAGE_STATUS_PATH + messageId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_messageStatusNotFound() {
		final var messageId = "787a8826-fdad-472a-834a-7ad7590e7f83";
		setupCall()
			.withServicePath(MESSAGE_STATUS_PATH + messageId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test5_deliveryStatus() {
		final var deliveryId = "171f4bbe-68af-422b-bf71-2fbbdd565130";
		setupCall()
			.withServicePath(DELIVERY_STATUS_PATH + deliveryId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test6_deliveryStatusNotFound() {
		final var deliveryId = "787a8826-fdad-472a-834a-7ad7590e7f83";
		setupCall()
			.withServicePath(DELIVERY_STATUS_PATH + deliveryId)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}
}
