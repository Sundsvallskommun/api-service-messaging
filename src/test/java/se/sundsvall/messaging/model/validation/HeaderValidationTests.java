package se.sundsvall.messaging.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createHeader;

import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

class HeaderValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_withNoConstraintViolations() {
        var header = createHeader();

        var constraintViolations = validator.validate(header);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withNullName() {
        var header = createHeader(hdr -> hdr.setName(null));

        var constraintViolations = validator.validate(header);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("name");
        });
    }

    @Test
    void test_withBlankName() {
        var header = createHeader(hdr -> hdr.setName(" "));

        var constraintViolations = validator.validate(header);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("name");
        });
    }

    @Test
    void test_withNullValues() {
        var header = createHeader(hdr -> hdr.setValues(null));

        var constraintViolations = validator.validate(header);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be empty");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("values");
        });
    }

    @Test
    void test_withEmptyValues() {
        var header = createHeader(hdr -> hdr.setValues(List.of()));

        var constraintViolations = validator.validate(header);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be empty");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("values");
        });
    }
}