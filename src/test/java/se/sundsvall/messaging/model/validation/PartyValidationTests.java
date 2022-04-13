package se.sundsvall.messaging.model.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createParty;

import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

class PartyValidationTests {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void test_withNoConstraintViolations() {
        var party = createParty();

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withNullPartyId() {
        var party = createParty(p -> p.setPartyId(null));

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("not a valid UUID");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("partyId");
        });
    }

    @Test
    void test_withBlankPartyId() {
        var party = createParty(p -> p.setPartyId(" "));

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("not a valid UUID");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("partyId");
        });
    }

    @Test
    void test_withNonUuidPartyId() {
        var party = createParty(p -> p.setPartyId("not-a-uuid"));

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("not a valid UUID");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("partyId");
        });
    }

    @Test
    void test_withNullExternalReferences() {
        var party = createParty(p -> p.setExternalReferences(null));

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withEmptyExternalReferences() {
        var party = createParty(p -> p.setExternalReferences(List.of()));

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void test_withInvalidExternalReference() {
        var party = createParty(p -> p.getExternalReferences().get(0).setKey(""));

        var constraintViolations = validator.validate(party);

        assertThat(constraintViolations).hasSize(1);
        assertThat(constraintViolations.iterator().next()).satisfies(constraintViolation -> {
            assertThat(constraintViolation.getMessage()).startsWith("must not be blank");
            assertThat(constraintViolation.getPropertyPath().toString()).isEqualTo("externalReferences[0].key");
        });
    }
}