package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@Disabled // TODO: This will be modified and activated in UF-10434
@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/MessageAttachmentsIT/", classes = Application.class)
class MessageAttachmentsIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/messages/{messageId}/attachment/{fileName}";
	private static final String BINARY_RESPONSE_FILE = "";

	@Test
	void test1_readAttachment() throws Exception {
		final var messageId = "";
		final var fileName = "";
		setupCall()
			.withServicePath(SERVICE_PATH.formatted(messageId, fileName))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedBinaryResponse(BINARY_RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_readAttachmentNotFound() {
		final var messageId = "";
		final var fileName = "";
		setupCall()
			.withServicePath(SERVICE_PATH.formatted(messageId, fileName))
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();
	}
}
