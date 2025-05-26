package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;
import se.sundsvall.dept44.test.AbstractAppTest;

abstract class AbstractMessagingAppTest extends AbstractAppTest {

	protected static final String HEADER_ORIGIN = "x-origin";
	protected static final String HEADER_SENDER = "X-Sent-By";
	protected static final String ORIGIN = "Test-origin";
	protected static final String SENDER = "type=adAccount; joe01doe";
	protected static final String SENDER_VALUE = "joe01doe";
	protected static final String MUNICIPALITY_ID = "2281";
	protected static final String REQUEST_FILE = "request.json";
	protected static final String RESPONSE_FILE = "response.json";

	private static final ValidUuidConstraintValidator VALID_UUID_CONSTRAINT_VALIDATOR = new ValidUuidConstraintValidator();

	protected void assertValidUuid(final String string) {
		assertThat(VALID_UUID_CONSTRAINT_VALIDATOR.isValid(string)).isTrue();
	}
}
