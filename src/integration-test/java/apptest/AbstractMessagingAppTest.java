package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;
import se.sundsvall.dept44.test.AbstractAppTest;

abstract class AbstractMessagingAppTest extends AbstractAppTest {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());

	private static final ValidUuidConstraintValidator VALID_UUID_CONSTRAINT_VALIDATOR = new ValidUuidConstraintValidator();

	protected void assertValidUuid(final String string) {
		assertThat(string).satisfies(s -> assertThat(VALID_UUID_CONSTRAINT_VALIDATOR.isValid(s)).isTrue());
	}
}
