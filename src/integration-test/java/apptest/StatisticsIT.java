package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@WireMockAppTestSuite(files = "classpath:/StatisticsIT/", classes = Application.class)
@DirtiesContext
// The statistics count every message in the database, so the classes run before this one must not leave theirs
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class StatisticsIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/statistics";

	@Test
	void test1_successfulStatsWithSms() {
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

	@Test
	void test4_successfulStatisticsByDepartment() {
		setupCall()
			.withServicePath(SERVICE_PATH + "/delivery-status?department=SBK(Gatuavdelningen, Trafiksektionen)&origin=origin1&from=2024-01-25&to=2024-01-26")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
