package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;
import se.sundsvall.dept44.test.AbstractAppTest;

abstract class AbstractMessagingAppTest extends AbstractAppTest {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	protected final static String HEADER_ORIGIN = "x-origin";
	protected final static String HEADER_ISSUER = "x-issuer";
	protected static final String ORIGIN = "Test-origin";
	protected static final String ISSUER = "Test-issuer";
	protected static final String MUNICIPALITY_ID = "2281";
	protected static final String REQUEST_FILE = "request.json";
	protected static final String RESPONSE_FILE = "response.json";

	private static final ValidUuidConstraintValidator VALID_UUID_CONSTRAINT_VALIDATOR = new ValidUuidConstraintValidator();

	protected void assertValidUuid(final String string) {
		assertThat(string).satisfies(s -> assertThat(VALID_UUID_CONSTRAINT_VALIDATOR.isValid(s)).isTrue());
	}
}
