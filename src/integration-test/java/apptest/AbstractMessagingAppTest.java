package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;
import se.sundsvall.dept44.test.AbstractAppTest;

abstract class AbstractMessagingAppTest extends AbstractAppTest {

	protected static final String X_SENT_BY_HEADER = "X-Sent-By";
	protected static final String X_SENT_BY_HEADER_VALUE = "type=adAccount; joe01doe";
	protected static final String X_SENT_BY_HEADER_USER_NAME = "joe01doe";

	protected static final String X_ORIGIN_HEADER = "x-origin";
	protected static final String X_ORIGIN_HEADER_VALUE = "Test-origin";
	
	protected static final String MUNICIPALITY_ID = "2281";
	protected static final String ORGANIZATION_NUMBER = "2120002411";
	protected static final String REQUEST_FILE = "request.json";
	protected static final String RESPONSE_FILE = "response.json";

	private static final ValidUuidConstraintValidator VALID_UUID_CONSTRAINT_VALIDATOR = new ValidUuidConstraintValidator();

	protected void assertValidUuid(final String string) {
		assertThat(VALID_UUID_CONSTRAINT_VALIDATOR.isValid(string)).isTrue();
	}
}
