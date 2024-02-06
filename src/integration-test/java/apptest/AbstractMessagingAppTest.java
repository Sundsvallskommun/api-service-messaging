package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;
import se.sundsvall.dept44.test.AbstractAppTest;

abstract class AbstractMessagingAppTest extends AbstractAppTest {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());

	private static final ValidUuidConstraintValidator VALID_UUID_CONSTRAINT_VALIDATOR = new ValidUuidConstraintValidator();

	protected Optional<Duration> getVerificationDelay() {
		return Optional.empty();
	}

	@Override
	public boolean verifyAllStubs() {
		getVerificationDelay().ifPresent(verificationDelay -> {
			LOG.info("Waiting {} seconds before verification", verificationDelay.getSeconds());

			try {
				TimeUnit.SECONDS.sleep(verificationDelay.getSeconds());
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
		});

		return super.verifyAllStubs();
	}

	protected void assertValidUuid(final String s) {
		assertThat(s).satisfies(VALID_UUID_CONSTRAINT_VALIDATOR::isValid);
	}
}
