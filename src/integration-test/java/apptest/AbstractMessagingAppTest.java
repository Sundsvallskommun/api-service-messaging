package apptest;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.dept44.test.AbstractAppTest;

abstract class AbstractMessagingAppTest extends AbstractAppTest {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected Optional<Duration> getVerificationDelay() {
        return Optional.empty();
    }

    @Override
    public void verifyAllStubs() {
        getVerificationDelay().ifPresent(verificationDelay -> {
            LOG.info("Waiting {} seconds before verification", verificationDelay.getSeconds());

            try {
                TimeUnit.SECONDS.sleep(verificationDelay.getSeconds());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        super.verifyAllStubs();
    }
}
