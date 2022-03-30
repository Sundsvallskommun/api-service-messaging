package se.sundsvall.messaging.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

class ExternalReferenceValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_withNoConstraintViolations() {
        var externalReference = createExternalReference();

        var constraintViolations = validator.validate(externalReference);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withNullKey() {
        var externalReference = createExternalReference(extRef -> extRef.setKey(null));

        var constraintViolations = validator.validate(externalReference);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("key");
        });
    }

    @Test
    void test_withBlankKey() {
        var externalReference = createExternalReference(extRef -> extRef.setKey(" "));

        var constraintViolations = validator.validate(externalReference);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("key");
        });
    }

    @Test
    void test_withNullValue() {
        var externalReference = createExternalReference(extRef -> extRef.setValue(null));

        var constraintViolations = validator.validate(externalReference);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("value");
        });
    }

    @Test
    void test_withBlankValue() {
        var externalReference = createExternalReference(extRef -> extRef.setValue(" "));

        var constraintViolations = validator.validate(externalReference);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("value");
        });
    }
}
