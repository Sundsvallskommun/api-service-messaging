package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/StatisticsIT/", classes = Application.class)
class StatisticsIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/statistics";

	@Test
	void test1_successfulStatsWithSms() throws Exception {
		setupCall()
			.withServicePath(SERVICE_PATH + "?messageType=SMS&from=2024-01-25&to=2024-02-25")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_successfulDepartmentStats() {
		setupCall()
			.withServicePath(SERVICE_PATH + "/departments")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_successfulStatsWithOriginAndDepartment() {
		setupCall()
			.withServicePath(SERVICE_PATH + "/departments/SBK(Gatuavdelningen, Trafiksektionen)?origin=origin1&from=2024-01-25&to=2024-01-26")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
