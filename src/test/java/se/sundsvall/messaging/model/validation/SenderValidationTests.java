package se.sundsvall.messaging.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createSender;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

class SenderValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_withNoConstraintViolations() {
        var sender = createSender();

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withNullSmsName() {
        var sender = createSender(s -> s.getSms().setName(null));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("sms.name");
        });
    }

    @Test
    void test_withBlankSmsName() {
        var sender = createSender(s -> s.getSms().setName(""));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("sms.name");
        });
    }

    @Test
    void test_withTooLongSmsName() {
        var sender = createSender(s -> s.getSms().setName("x".repeat(12)));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("size must be between 0 and 11");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("sms.name");
        });
    }

    @Test
    void test_withNullEmailName() {
        var sender = createSender(s -> s.getEmail().setName(null));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("email.name");
        });
    }

    @Test
    void test_withBlankEmailName() {
        var sender = createSender(s -> s.getEmail().setName(""));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("email.name");
        });
    }

    @Test
    void test_withNullEmailAddress() {
        var sender = createSender(s -> s.getEmail().setAddress(null));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("email.address");
        });
    }

    @Test
    void test_withBlankEmailAddress() {
        var sender = createSender(s -> s.getEmail().setAddress(""));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("email.address");
        });
    }

    @Test
    void test_withInvalidEmailAddress() {
        var sender = createSender(s -> s.getEmail().setAddress("not-an-email-address"));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must be a well-formed email address");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("email.address");
        });
    }

    @Test
    void test_withNullEmailReplyTo() {
        var sender = createSender(s -> s.getEmail().setReplyTo(null));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withBlankEmailReplyTo() {
        var sender = createSender(s -> s.getEmail().setReplyTo(""));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withInvalidEmailReplyTo() {
        var sender = createSender(s -> s.getEmail().setReplyTo("not-an-email-address"));

        var constraintViolations = validator.validate(sender);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must be a well-formed email address");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("email.replyTo");
        });
    }
}