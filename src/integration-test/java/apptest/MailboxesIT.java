package apptest;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;

@WireMockAppTestSuite(files = "classpath:/MailboxesIT/", classes = Application.class)
class MailboxesIT extends AbstractMessagingAppTest {
	
	private static final String ORGANIZATION_NUMBER = "2120002411";
	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/mailboxes";
	
	@Test
	void test1_successfulRequest() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(X_SENT_BY_HEADER, X_SENT_BY_HEADER_VALUE)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
	
	@Test
	void test2_internalServerErrorFromDigitalMail() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(X_SENT_BY_HEADER, X_SENT_BY_HEADER_VALUE)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(BAD_GATEWAY)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
